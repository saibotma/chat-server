package clientapi

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import testutil.ServerTestEnvironment
import testutil.serverTest
import java.time.Instant.now
import java.util.*

class ClientApiJwtAuthenticationTests {
    @Test
    fun `grants access with a valid jwt`() {
        serverTest {
            val (_, user) = upsertUser()
            val response = sendRequest(createUserToken(userId = user!!.id).second!!.jwt)
            response.status() shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `returns an error when the token has an unknown subject`() {
        serverTest {
            val clientApiConfig: ClientApiConfig by di.instance()
            val response = sendRequest(
                JWT.create().withSubject("invalid subject").sign(Algorithm.HMAC256(clientApiConfig.jwtSecret))
            )
            response.status() shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    fun `returns an error when the token is expired`() {
        serverTest {
            val (_, user) = upsertUser()
            val clientApiConfig: ClientApiConfig by di.instance()
            val response = sendRequest(
                JWT.create().withSubject(user!!.id).withExpiresAt(Date.from(now().minusSeconds(60)))
                    .sign(Algorithm.HMAC256(clientApiConfig.jwtSecret))
            )
            response.status() shouldBe HttpStatusCode.Unauthorized
        }
    }

    private fun ServerTestEnvironment.sendRequest(jwt: String): TestApplicationResponse {
        return testApplicationEngine.handleRequest(HttpMethod.Post, "/client/graphql") {
            val objectMapper = jacksonObjectMapper()
            addHeader("Content-Type", "application/json")
            addHeader("Authorization", "Bearer $jwt")
            val body = objectMapper.writeValueAsString(mapOf("query" to "{ channels { id } }"))
            setBody(body)
        }.response
    }
}
