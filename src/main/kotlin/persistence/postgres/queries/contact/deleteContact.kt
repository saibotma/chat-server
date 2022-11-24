package persistence.postgres.queries.contact

import clientapi.UserId
import org.jooq.impl.DSL.value
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.CONTACT

/**
 * Deletes in both directions. That means the values of [userId1] and [userId2] can be interchanged.
 */
fun KotlinTransactionContext.deleteContact(userId1: UserId, userId2: UserId) {
    db.deleteFrom(CONTACT)
        .where(areContacts(contact = CONTACT, userId1 = value(userId1.value), userId2 = value(userId2.value)))
        .execute()
}
