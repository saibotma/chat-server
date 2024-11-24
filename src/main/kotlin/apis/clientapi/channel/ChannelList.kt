package apis.clientapi.channel

import apis.clientapi.Channels
import apis.clientapi.models.ChannelCreatePayload
import apis.clientapi.models.toChannel
import apis.clientapi.models.toChannelMember
import config.WebhookConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.channel.insertChannel
import persistence.postgres.queries.insertChannelMembers
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.createChannel(
    location: Channels,
    postgres: KotlinDslContext,
    client: HttpClient,
    config: WebhookConfig,
) {
    val channel: ChannelCreatePayload = call.receive()

    val response = client.post {
        url("${config.baseUrl}/permissions/add-channel-member")
        setBody(channel.members)
    }
    if (!response.status.isSuccess()) {
        call.respond(HttpStatusCode.BadRequest)
        return
    }

    val channelId = UUID.randomUUID()
    postgres.transaction {
        insertChannel(channel.toChannel(id = channelId))
        insertChannelMembers(channel.members.map { it.toChannelMember(channelId = channelId) })
    }

    call.respond(HttpStatusCode.Created, mapOf("id" to channelId))
}
