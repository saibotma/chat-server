package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.updateChannel
import models.ChannelMeta

// TODO(saibotma): Finalize API & test
suspend fun PipelineContext<Unit, ApplicationCall>.updateChannelMeta(
    location: ChannelList.ChannelDetails.ChannelMetaDetails,
    database: KotlinDslContext
) {
    val meta = call.receive<ChannelMeta>()
    database.transaction { updateChannel(meta) }
    call.respond(HttpStatusCode.NoContent)
}
