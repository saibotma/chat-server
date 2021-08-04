package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import platformapi.models.ChannelWritePayload
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.updateChannel(
    location: ChannelList.ChannelDetails,
    database: KotlinDslContext
) {
    val channel = call.receive<ChannelWritePayload>()

    database.transaction {
        updateChannel(id = location.channelId, name = channel.name, isManaged = channel.isManaged)
    }
    call.respond(HttpStatusCode.NoContent)
}

suspend fun PipelineContext<Unit, ApplicationCall>.deleteChannel(
    location: ChannelList.ChannelDetails,
    database: KotlinDslContext
) {
    database.transaction { deleteChannel(location.channelId) }
    call.respond(HttpStatusCode.NoContent)
}
