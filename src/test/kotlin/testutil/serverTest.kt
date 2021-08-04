package testutil

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import error.ApiError
import error.ApiException
import error.duplicate
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import module
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.subDI
import platformapi.DatabaseTestEnvironment
import platformapi.PlatformApiConfig
import platformapi.setupTestDependencies

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
        val environment = ServerTestEnvironment(this).apply { resetDatabase() }
        runBlocking { test(environment) }
    }
}

class ServerTestEnvironment(val testApplicationEngine: TestApplicationEngine) :
    DatabaseTestEnvironment(testApplicationEngine.application.closestDI()) {
    val di = testApplicationEngine.application.closestDI()
    val objectMapper: ObjectMapper by di.instance()

    fun upsertChannel(
        channel: DetailedChannel, response: TestApplicationResponse.(DetailedChannel) -> Unit = { ensureSuccess() }
    ): DetailedChannel {
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
