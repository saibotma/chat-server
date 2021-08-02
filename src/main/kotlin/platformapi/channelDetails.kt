package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.insertChannel
import persistence.postgres.queries.updateChannel
import platformapi.models.ChannelWrite
import util.Fallible

suspend fun PipelineContext<Unit, ApplicationCall>.upsertChannel(
    location: ChannelList.ChannelDetails,
    database: KotlinDslContext
) {
    val channel = call.receive<ChannelWrite>()
    database.transaction {
        when (insertChannel(channel)) {
            is Fallible.Failure -> {
                updateChannel(channel)
                call.respond(HttpStatusCode.NoContent)
            }
            is Fallible.Success -> call.respond(HttpStatusCode.Created)
        }
    }
}
