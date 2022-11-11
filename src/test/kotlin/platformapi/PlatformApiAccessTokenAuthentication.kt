package platformapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import testutil.mockedUser
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.serverTest
import java.util.UUID.randomUUID

class PlatformApiAccessTokenAuthentication {
    @Test
    fun `grants access with a valid token`() {
        serverTest {
            val platformApiConfig: PlatformApiConfig by di.instance()
            val response = sendRequest(platformApiConfig.accessToken)
            response.status shouldBe HttpStatusCode.Created
        }
    }

    @Test
    fun `returns an error when the token is invalid`() {
        serverTest {
            val response = sendRequest("invalid token")
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    private suspend fun ServerTestEnvironment.sendRequest(token: String): HttpResponse {
        val userId = randomUUID()
        val user = mockedUser()
        return client.put("/platform/users/$userId") {
            val objectMapper = jacksonObjectMapper()
            header("Content-Type", "application/json")
            header("X-Chat-Server-Platform-Api-Access-Token", token)
            val body = objectMapper.writeValueAsString(user)
            setBody(body)
        }
    }
}
