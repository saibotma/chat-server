package graphql

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import testutil.mockedChannelMember
import testutil.serverTest

class GraphQLInstallationTests {

    @Test
    fun `is correctly installed`() {
        serverTest {
            val objectMapper = jacksonObjectMapper()
            val (_, user) = createUser()
            val (_, channel) = createChannel()
            addMember(channelId = channel!!.id, mockedChannelMember(userId = user!!.id))
            val response = testApplicationEngine.handleRequest(HttpMethod.Post, "/client/graphql") {
                addHeader("Content-Type", "application/json")
                addHeader("Authorization", "Bearer ${createUserToken(user.id).second!!.jwt}")
                val body = objectMapper.writeValueAsString(mapOf("query" to "{ channels { id } }"))
                setBody(body)
            }.response

            response.status() shouldBe HttpStatusCode.OK
            val json = objectMapper.readValue<Map<String, Any>>(response.content!!)
            json shouldNotContainKey "errors"
        }
    }
}
