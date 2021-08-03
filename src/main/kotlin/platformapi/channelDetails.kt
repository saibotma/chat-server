package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import platformapi.models.ChannelWrite
import util.Fallible

suspend fun PipelineContext<Unit, ApplicationCall>.upsertChannel(
    location: ChannelList.ChannelDetails,
    database: KotlinDslContext
) {
    val channel = call.receive<ChannelWrite>()
    // Have two transactions as postgres does not like reusing a transaction that
    // had errors. (https://stackoverflow.com/questions/10399727/psqlexception-current-transaction-is-aborted-commands-ignored-until-end-of-tra)
    when (database.transaction {
        val insertResult = insertChannel(channel.meta)
        insertMembers(channel.members)
        insertResult
    }) {
        is Fallible.Failure -> {
            database.transaction {
                updateChannel(channel.meta)
                deleteMembersOf(location.channelId)
                insertMembers(channel.members)
            }
            call.respond(HttpStatusCode.NoContent)
        }
        is Fallible.Success -> call.respond(HttpStatusCode.Created)
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.deleteChannel(
    location: ChannelList.ChannelDetails,
    database: KotlinDslContext
) {
    database.transaction { deleteChannel(location.channelId) }
    call.respond(HttpStatusCode.NoContent)
}
