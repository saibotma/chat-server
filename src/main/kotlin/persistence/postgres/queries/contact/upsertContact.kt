package persistence.postgres.queries.contact

import clientapi.UserId
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.CONTACT

fun KotlinTransactionContext.upsertContact(userId1: UserId, userId2: UserId) {
    db.insertInto(CONTACT)
        .set(CONTACT.USER_ID_1, userId1.value)
        .set(CONTACT.USER_ID_2, userId2.value)
        .onDuplicateKeyIgnore()
        .execute()
}
