package graphql

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import testutil.mockedChannelMemberWrite
import testutil.servertest.post.addMember
import testutil.servertest.post.createChannel
import testutil.servertest.post.createUserToken
import testutil.servertest.put.upsertUser
import testutil.servertest.serverTest

class GraphQLInstallationTests {

    @Test
    fun `is correctly installed`() {
        serverTest {
            val objectMapper = jacksonObjectMapper()
            val (_, user) = upsertUser()
            val (_, channel) = createChannel()
            addMember(channelId = channel!!.id, mockedChannelMemberWrite(userId = user!!.id))
            val response = client.post("/client/graphql") {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer ${createUserToken(user.id).second!!.jwt}")
                val body = objectMapper.writeValueAsString(mapOf("query" to "{ channels { id } }"))
                setBody(body)
            }

            response.status shouldBe HttpStatusCode.OK
            val json = objectMapper.readValue<Map<String, Any>>(response.bodyAsText())
            json shouldNotContainKey "errors"
        }
    }
}
