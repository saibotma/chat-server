package persistence.postgres.queries.contact

import clientapi.UserId
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.CONTACT

fun KotlinTransactionContext.updateContact(userId1: UserId, userId2: UserId, isApproved: Boolean): Int {
    return db.update(CONTACT)
        .set(CONTACT.IS_APPROVED, isApproved)
        .where(CONTACT.USER_ID_1.eq(userId1.value))
        .and(CONTACT.USER_ID_2.eq(userId2.value))
        .execute()
}
