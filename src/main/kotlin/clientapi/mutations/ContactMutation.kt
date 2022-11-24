package clientapi.mutations

import clientapi.AuthContext
import clientapi.UserId
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.contact.deleteContact
import persistence.postgres.queries.contact.insertContact
import persistence.postgres.queries.contact.updateContact

// TODO(saibotma): Remove all created_at and updated_at setters in code, because they got replaced by triggers.

class ContactMutation(private val database: KotlinDslContext) {
    suspend fun addContact(context: AuthContext, userId: String) {
        // TODO(saibotma): Send notification.
        database.transaction {
            val updatedRows = updateContact(userId1 = UserId(userId), userId2 = context.userId, isApproved = true)
            if (updatedRows == 0) {
                insertContact(userId1 = context.userId, userId2 = UserId(userId), isApproved = false)
            }
        }
    }

    suspend fun removeContact(context: AuthContext, userId: String) {
        database.transaction {
            deleteContact(userId1 = context.userId, userId2 = UserId(userId))
        }
    }
}
