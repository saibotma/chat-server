package platformapi

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.jooq.tables.pojos.Contact
import persistence.postgres.queries.contact.insertContact

suspend fun PipelineContext<Unit, ApplicationCall>.upsertContact(
    location: ContactList.ContactDetails,
    database: KotlinDslContext
) {
    database.transaction {
        insertContact(contact = Contact(userId_1 = location.userId1, userId_2 = location.userId2))
    }
    call.respond(HttpStatusCode.NoContent)
}
