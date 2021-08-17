package platformapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import testutil.ServerTestEnvironment
import testutil.mockedUser
import testutil.serverTest
import java.util.*
import java.util.UUID.randomUUID

class PlatformApiAccessTokenAuthentication {
    @Test
    fun `grants access with a valid token`() {
        serverTest {
            val platformApiConfig: PlatformApiConfig by di.instance()
            val response = sendRequest(platformApiConfig.accessToken)
            response.status() shouldBe HttpStatusCode.Created
        }
    }

    @Test
    fun `returns an error when the token is invalid`() {
        serverTest {
            val response = sendRequest("invalid token")
            response.status() shouldBe HttpStatusCode.Unauthorized
        }
    }

    private fun ServerTestEnvironment.sendRequest(token: String): TestApplicationResponse {
        val userId = randomUUID()
        val user = mockedUser()
        return testApplicationEngine.handleRequest(HttpMethod.Put, "/platform/users/$userId") {
            val objectMapper = jacksonObjectMapper()
            addHeader("Content-Type", "application/json")
            addHeader("X-Chat-Server-Platform-Api-Access-Token", token)
            val body = objectMapper.writeValueAsString(user)
            setBody(body)
        }.response
    }
}
