package testutil

import chatServer
import clientapi.mutations.ChannelMutation
import clientapi.mutations.MessageMutation
import clientapi.mutations.PushMutation
import clientapi.queries.ChannelQuery
import clientapi.queries.MessageQuery
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import di.setupDi
import error.PlatformApiError
import error.PlatformApiException
import error.duplicate
import error.resourceNotFound
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import models.*
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import persistence.jooq.KotlinDslContext
import platformapi.PlatformApiConfig
import java.util.*
import java.util.UUID.randomUUID

class TestRollbackException : Exception()

fun serverTest(
    bindDependencies: DI.MainBuilder.() -> Unit = {},
    test: suspend ServerTestEnvironment.() -> Unit
) {
    withTestApplication({
        chatServer(DI {
            setupDi()
            setupTestDependencies()
            bindDependencies()
        })
    }) {
        val di = application.closestDI()
        val database: DSLContext by di.instance()
        try {
            database.transaction { config ->
                val kotlinDslContext: KotlinDslContext by di.instance()
                kotlinDslContext.overrideDSLContext = DSL.using(config)
                val environment = ServerTestEnvironment(this)
                runBlocking { test(environment) }
                throw TestRollbackException()
            }
        } catch (e: DataAccessException) {
            if (e.cause !is TestRollbackException) throw e
        }
    }

}

class ServerTestEnvironment(val testApplicationEngine: TestApplicationEngine) :
    DatabaseTestEnvironment(testApplicationEngine.application.closestDI()) {
    val di = testApplicationEngine.application.closestDI()
    val objectMapper: ObjectMapper by di.instance()

    val channelQuery: ChannelQuery by di.instance()
    val messageQuery: MessageQuery by di.instance()
    val channelMutation: ChannelMutation by di.instance()
    val messageMutation: MessageMutation by di.instance()
    val pushMutation: PushMutation by di.instance()

    fun createChannel(
        channel: ChannelWritePayload = mockedChannelWrite(),
        response: TestApplicationResponse.(ChannelWritePayload, ChannelReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
    ): Pair<ChannelWritePayload, ChannelReadPayload?> = post(channel, "/platform/channels", response)

    fun updateChannel(
        id: UUID,
        channel: ChannelWritePayload,
        response: TestApplicationResponse.(ChannelWritePayload, ChannelReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
    ): Pair<ChannelWritePayload, ChannelReadPayload?> {
        return put(channel, "/platform/channels/$id", response)
    }

    fun deleteChannel(
        id: UUID,
        response: TestApplicationResponse.() -> Unit = { ensureNoContent() }
    ) = delete(path = "/platform/channels/$id", response)

    fun addMember(
        channelId: UUID,
        member: ChannelMemberWritePayload,
        response: TestApplicationResponse.(ChannelMemberWritePayload, ChannelMemberReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
    ): Pair<ChannelMemberWritePayload, ChannelMemberReadPayload?> {
        return post(member, "/platform/channels/$channelId/members", response)
    }

    fun updateMember(
        channelId: UUID,
        member: ChannelMemberWritePayload,
        response: TestApplicationResponse.(ChannelMemberWritePayload, ChannelMemberReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
    ): Pair<ChannelMemberWritePayload, ChannelMemberReadPayload?> {
        return put(member, "/platform/channels/$channelId/members/${member.userId}", response)
    }

    fun deleteMember(
        channelId: UUID,
        userId: String,
        response: TestApplicationResponse.() -> Unit = { ensureNoContent() }
    ) = delete(path = "/platform/channels/$channelId/members/$userId", response)

    fun setMembers(
        channelId: UUID,
        members: List<ChannelMemberWritePayload>,
        response: TestApplicationResponse.(List<ChannelMemberWritePayload>, List<ChannelMemberReadPayload>?) -> Unit = { _, _ -> ensureSuccess() }
    ): Pair<List<ChannelMemberWritePayload>, List<ChannelMemberReadPayload>?> {
        return put(members, "/platform/channels/$channelId/members", response)
    }

    fun upsertUser(
        id: String = randomUUID().toString(),
        user: UserWritePayload = mockedUser(),
        response: TestApplicationResponse.(UserWritePayload, UserReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
    ): Pair<UserWritePayload, UserReadPayload?> {
        return put(user, "/platform/users/$id", response)
    }

    fun createUserToken(
        userId: String,
        response: TestApplicationResponse.(Unit, UserToken?) -> Unit = { _, _ -> ensureSuccess() }
    ): Pair<Unit, UserToken?> {
        return post(Unit, "/platform/users/$userId/tokens", response)
    }

    fun deleteUser(
        id: String,
        response: TestApplicationResponse.() -> Unit = { ensureNoContent() }
    ) = delete(path = "/platform/users/$id", response)

    inline fun <T, reified R> post(
        resource: T,
        path: String,
        response: TestApplicationResponse.(T, R?) -> Unit
    ): Pair<T, R?> {
        var result: R?
        testApplicationEngine.handleRequest(HttpMethod.Post, path) {
            addApiKeyAuthentication()
            setJsonBody(resource, objectMapper)
        }.response.apply {
            result =
                if (status()?.isSuccessWithContent() == true) content?.let { objectMapper.readValue<R>(it) } else null
            response(resource, result)
        }
        return resource to result
    }

    inline fun <T, reified R> put(
        resource: T,
        path: String,
        response: TestApplicationResponse.(T, R?) -> Unit
    ): Pair<T, R?> {
        var result: R?
        testApplicationEngine.handleRequest(HttpMethod.Put, path) {
            addApiKeyAuthentication()
            setJsonBody(resource, objectMapper)
        }.response.apply {
            result =
                if (status()?.isSuccessWithContent() == true) content?.let { objectMapper.readValue<R>(it) } else null
            response(resource, result)
        }
        return resource to result
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

    fun <T> TestApplicationRequest.setJsonBody(body: T, objectMapper: ObjectMapper) {
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
        val expectedError = PlatformApiException.duplicate(duplicatePropertyName, duplicatePropertyValue).error
        asApiError() shouldBe expectedError

        status() shouldBe HttpStatusCode.BadRequest
    }

    fun TestApplicationResponse.ensureHasContent() {
        content shouldNotBe null
        status() shouldBe HttpStatusCode.OK
    }

    fun TestApplicationResponse.ensureResourceNotFound() {
        asApiError() shouldBe PlatformApiException.resourceNotFound().error
        status() shouldBe HttpStatusCode.NotFound
    }

    fun TestApplicationResponse.asApiError(): PlatformApiError {
        return objectMapper.readValue(content!!)
    }

    fun HttpStatusCode.isSuccessWithContent() = this == HttpStatusCode.OK || this == HttpStatusCode.Created
}
