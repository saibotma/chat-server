package platformapi

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import models.ChannelWritePayload
import models.toChannelRead

suspend fun PipelineContext<Unit, ApplicationCall>.updateChannel(
    location: ChannelList.ChannelDetails,
    database: KotlinDslContext
) {
    val channel = call.receive<ChannelWritePayload>()
    val channelId = location.channelId

    val result = database.transaction {
        updateChannel(id = channelId, name = channel.name, isManaged = channel.isManaged)
        getChannel(channelId)!!.toChannelRead()
    }
    call.respond(HttpStatusCode.OK, result)
}

suspend fun PipelineContext<Unit, ApplicationCall>.deleteChannel(
    location: ChannelList.ChannelDetails,
    database: KotlinDslContext
) {
    database.transaction { deleteChannel(location.channelId) }
    call.respond(HttpStatusCode.NoContent)
}
