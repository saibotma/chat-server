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
    // Have two transactions as postgres does not like reusing a transaction that
    // had errors. (https://stackoverflow.com/questions/10399727/psqlexception-current-transaction-is-aborted-commands-ignored-until-end-of-tra)
    when (database.transaction { insertChannel(channel) }) {
        is Fallible.Failure -> {
            database.transaction { updateChannel(channel) }
            call.respond(HttpStatusCode.NoContent)
        }
        is Fallible.Success -> call.respond(HttpStatusCode.Created)
    }
}
