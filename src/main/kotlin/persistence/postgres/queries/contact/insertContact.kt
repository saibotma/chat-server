package persistence.postgres.queries.contact

import clientapi.UserId
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.CONTACT

fun KotlinTransactionContext.insertContact(userId1: UserId, userId2: UserId, isApproved: Boolean) {
    db.insertInto(CONTACT)
        .set(CONTACT.USER_ID_1, userId1.value)
        .set(CONTACT.USER_ID_2, userId2.value)
        .set(CONTACT.IS_APPROVED, isApproved)
        .execute()
}
