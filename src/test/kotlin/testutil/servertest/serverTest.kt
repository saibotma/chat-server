package testutil.servertest

import chatServer
import clientapi.mutations.ChannelMutation
import clientapi.mutations.MessageMutation
import clientapi.mutations.PushMutation
import clientapi.queries.ChannelQuery
import clientapi.queries.MessageQuery
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import models.*
import org.flywaydb.core.Flyway
import org.kodein.di.*
import platformapi.PlatformApiConfig
import testutil.DatabaseTestEnvironment
import testutil.handleCleanUp
import testutil.restoreDatabase
import testutil.setupTestDependencies
import java.util.*

typealias ServerTestResponse = HttpResponse
typealias BindDependencies = DI.MainBuilder.() -> Unit

fun serverTest(
    shouldUseTransaction: Boolean = true,
    bindDependencies: BindDependencies = {},
    test: suspend ServerTestEnvironment.() -> Unit
) {
    testApplication {
        // Need to use the test configuration because the config should not load modules because we do that
        // by hand in application block below.
        val config = ApplicationConfig(configPath = null)
        val di = DI {
            setupTestDependencies(config)
            bindDependencies()
        }

        environment { this.config = config }

        application { chatServer(di = di) }

        val client = createClient {
            install(ContentNegotiation) {
                jackson { di.direct.instance<ObjectMapper.() -> Unit>()() }
            }
            followRedirects = false
        }

        val flyway: Flyway by di.instance()
        val environment = ServerTestEnvironment(client = client, di = di)

        handleCleanUp(flyway)

        if (shouldUseTransaction) {
            environment.scopedTest(test)
        } else {
            try {
                runBlocking { test(environment) }
            } finally {
                restoreDatabase(flyway)
            }
        }
    }
}

class ServerTestEnvironment(val client: HttpClient, di: DI) :
    DatabaseTestEnvironment(di = di) {
    private val platformApiConfig: PlatformApiConfig by di.instance()
    val objectMapper: ObjectMapper by di.instance()

    val channelQuery: ChannelQuery by di.instance()
    val messageQuery: MessageQuery by di.instance()
    val channelMutation: ChannelMutation by di.instance()
    val messageMutation: MessageMutation by di.instance()
    val pushMutation: PushMutation by di.instance()

    fun HttpRequestBuilder.addApiKeyAuthentication() {
        header("X-Chat-Server-Platform-Api-Access-Token", platformApiConfig.accessToken)
    }

    fun <T> TestApplicationRequest.setJsonBody(body: T, objectMapper: ObjectMapper) {
        addHeader("Content-Type", "application/json")
        setBody(body.asString(objectMapper))
    }

    private fun <T> T.asString(objectMapper: ObjectMapper): String {
        return objectMapper.writeValueAsString(this)
    }

    fun HttpStatusCode.isSuccessWithContent() = this == HttpStatusCode.OK || this == HttpStatusCode.Created

    @JvmName("scopedTestServer")
    fun scopedTest(block: suspend ServerTestEnvironment.() -> Unit) {
        return scopedTest(this, block)
    }
}
