package platformapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.saibotma.persistence.postgres.jooq.tables.Channel.Companion.CHANNEL
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Channel
import error.ApiError
import error.ApiException
import error.duplicate
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.*
import io.ktor.server.testing.*
import di.setupKodein
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import module
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.*
import org.kodein.di.ktor.closestDI
import org.testcontainers.containers.PostgreSQLContainer
import persistence.postgres.ChatServerPostgres
import persistence.postgres.PostgresConfig
import platformapi.models.ChannelWrite
import platformapi.models.toChannelWrite
import testutil.DatabaseTest
import testutil.mockedChannelWrite

class ChannelDetailsTests : DatabaseTest() {
    @Nested
    inner class UpsertChannelTests {
        @Test
        fun `creates a channel when no one exists yet`() {
            serverTest {
                val channel = upsertChannel(mockedChannelWrite())

                with(getChannels().map { it.toChannelWrite() }.toList()) {
                    shouldHaveSize(1)
                    first() shouldBe channel
                }
            }
        }

        @Test
        fun `updates a channel when it already exists`() {

        }
    }
}

fun DI.MainBuilder.setupTestDependencies() {
    val postgresContainer = PostgreSQLContainer<Nothing>()
    bind<PostgresConfig>(overrides = true) with singleton {
        PostgresConfig(
            user = postgresContainer.username,
            password = postgresContainer.password,
            serverName = postgresContainer.host,
            port = postgresContainer.firstMappedPort,
            db = postgresContainer.databaseName
        )
    }
}

fun serverTest(
    bindDependencies: DI.MainBuilder.() -> Unit = {},
    test: suspend ServerTestEnvironment.() -> Unit
) {
    withTestApplication({
        module()
        subDI(parentDI = closestDI()) {
            setupTestDependencies()
            bindDependencies()
        }
    }) {
        val kodein = application.closestDI()
        val objectMapper: ObjectMapper by kodein.instance()
        print(objectMapper.writeValueAsString("klajsldkjfasdf"))

        val environment = ServerTestEnvironment(this).apply { resetDatabase() }
        runBlocking { test(environment) }
    }
}

class ServerTestEnvironment(val testApplicationEngine: TestApplicationEngine) :
    DatabaseTestEnvironment(testApplicationEngine.application.closestDI()) {
    val di = testApplicationEngine.application.closestDI()
    val objectMapper: ObjectMapper by di.instance()

    fun upsertChannel(
        channel: ChannelWrite, response: TestApplicationResponse.(ChannelWrite) -> Unit = { ensureSuccess() }
    ): ChannelWrite {
        return put(channel, "/platform/channels/${channel.id}", response)
    }

    fun <T> put(
        resource: T,
        path: String,
        response: TestApplicationResponse.(T) -> Unit
    ): T {
        testApplicationEngine.handleRequest(HttpMethod.Put, path) {
            addApiKeyAuthentication()
            setJsonBody(resource, objectMapper)
        }.response.apply { response(resource) }
        return resource
    }

    inline fun <reified T> get(
        path: String,
        response: TestApplicationResponse.(T?) -> Unit
    ) {
        testApplicationEngine.handleRequest(HttpMethod.Get, path) {
            addApiKeyAuthentication()
        }.response.apply {
            response(if (status()?.isSuccess() == true) content?.let { objectMapper.readValue<T>(it) } else null)
        }
    }

    fun delete(
        path: String,
        response: TestApplicationResponse.() -> Unit
    ) {
        testApplicationEngine.handleRequest(HttpMethod.Delete, path) {
            addApiKeyAuthentication()
        }.response.apply { response() }
    }

    fun TestApplicationRequest.addApiKeyAuthentication() {
        val platformApiConfig: PlatformApiConfig by closestDI { this.call.application }.instance()
        addHeader("X-Chat-Server-Platform-Api-Access-Token", platformApiConfig.accessToken)
    }

    private fun <T> TestApplicationRequest.setJsonBody(body: T, objectMapper: ObjectMapper) {
        addHeader("Content-Type", "application/json")
        setBody(body.asString(objectMapper))
    }

    private fun <T> T.asString(objectMapper: ObjectMapper): String {
        return objectMapper.writeValueAsString(this)
    }

    fun TestApplicationResponse.ensureSuccess() {
        status() shouldBeIn listOf(HttpStatusCode.OK, HttpStatusCode.NoContent, HttpStatusCode.Created)
    }

    fun TestApplicationResponse.ensureNoContent() {
        content shouldBe null
        status() shouldBe HttpStatusCode.NoContent
    }

    fun TestApplicationResponse.ensureBadRequestWithDuplicate(
        duplicatePropertyName: String,
        duplicatePropertyValue: String
    ) {
        val expectedError = ApiException.duplicate(duplicatePropertyName, duplicatePropertyValue).error
        content?.asApiError(objectMapper) shouldBe expectedError

        status() shouldBe HttpStatusCode.BadRequest
    }

    fun TestApplicationResponse.ensureHasContent() {
        content shouldNotBe null
        status() shouldBe HttpStatusCode.OK
    }

    fun String.asApiError(objectMapper: ObjectMapper): ApiError {
        return objectMapper.readValue(this)
    }
}

fun databaseTest(
    bindDependencies: DI.MainBuilder.() -> Unit = {},
    test: suspend DatabaseTestEnvironment.() -> Unit
) {
    val kodein = DI {
        setupKodein()
        setupTestDependencies()
        bindDependencies()
    }
    val environment = DatabaseTestEnvironment(kodein)
    environment.resetDatabase()
    runBlocking { test(environment) }
}


open class DatabaseTestEnvironment(private val di: DI) {
    val postgres: ChatServerPostgres by di.instance()
    val database = postgres.kotlinDslContext
    fun resetDatabase() = di.direct.instance<ChatServerPostgres>().apply { clean(); runMigration() }

    suspend fun getChannels(): Flow<Channel> {
        return database.transaction {
            db.selectFrom(CHANNEL).asFlow().map { it.into(Channel::class.java) }
        }
    }
}
