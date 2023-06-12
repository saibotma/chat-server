package platformapi

import clientapi.UserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.contact.deleteContact
import persistence.postgres.queries.contact.upsertContact

suspend fun PipelineContext<Unit, ApplicationCall>.upsertContact(
    location: ContactList.ContactDetails,
    database: KotlinDslContext
) {
    database.transaction {
        upsertContact(userId1 = UserId(location.userId1), userId2 = UserId(location.userId2))
    }
    call.respond(HttpStatusCode.NoContent)
}

// TODO(saibotma): Test this.
suspend fun PipelineContext<Unit, ApplicationCall>.deleteContact(
    location: ContactList.ContactDetails,
    database: KotlinDslContext
) {
    database.transaction {
        deleteContact(userId1 = UserId(location.userId1), userId2 = UserId(location.userId2))
    }
    call.respond(HttpStatusCode.NoContent)
}
