package platformapi

import clientapi.UserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.contact.insertContact

suspend fun PipelineContext<Unit, ApplicationCall>.upsertContact(
    location: ContactList.ContactDetails,
    database: KotlinDslContext
) {
    database.transaction {
        insertContact(userId1 = UserId(location.userId1), userId2 = UserId(location.userId2), isApproved = true)
    }
    call.respond(HttpStatusCode.NoContent)
}
