package apis.clientapi.channel

import apis.clientapi.Channels
import apis.clientapi.models.ChannelUpdatePayload
import graphqlclientapi.AuthContext
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import models.channelcontent.ChannelNameUpdate
import persistence.jooq.KotlinDslContext
import persistence.jooq.enums.ChannelContentType
import persistence.postgres.queries.channel.isAdminOfChannel
import persistence.postgres.queries.channel.updateChannel
import persistence.postgres.queries.channelcontent.insertChannelContent
import persistence.postgres.queries.deleteChannel
import persistence.postgres.queries.getChannel
import util.toOptional

suspend fun PipelineContext<Unit, ApplicationCall>.updateChannel(
    location: Channels.Channel,
    postgres: KotlinDslContext,
) {
    val authContext = call.principal<AuthContext>()!!
    val channel: ChannelUpdatePayload = call.receive()
    val isChannelAdmin =
        postgres.transaction { isAdminOfChannel(channelId = location.channelId, userId = authContext.userId) }
    if (!isChannelAdmin) {
        call.respond(HttpStatusCode.BadRequest)
    }

    val oldChannel = postgres.transaction { getChannel(channelId = location.channelId) } ?: TODO("Throw exception")
    postgres.transaction {
        updateChannel(id = location.channelId, name = channel.name.toOptional())
        if (oldChannel.name != channel.name) {
            insertChannelContent(
                channelId = location.channelId,
                type = ChannelContentType.channel_name_update,
                data = ChannelNameUpdate(name = channel.name),
            )
        }
    }

    call.respond(HttpStatusCode.NoContent)
}

suspend fun PipelineContext<Unit, ApplicationCall>.deleteChannel(
    location: Channels.Channel,
    postgres: KotlinDslContext,
) {
    // TODO(saibotma): Check if the user is admin.
    if (false) {
        call.respond(HttpStatusCode.BadRequest)
    }

    postgres.transaction {
        deleteChannel(channelId = location.channelId)
    }

    call.respond(HttpStatusCode.NoContent)
}

