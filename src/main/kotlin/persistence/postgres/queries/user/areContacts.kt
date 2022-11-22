package persistence.postgres.queries.user

import clientapi.UserId
import org.jooq.Condition
import org.jooq.impl.DSL.exists
import org.jooq.impl.DSL.selectFrom
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.Contact.Companion.CONTACT
import persistence.jooq.tables.references.CHANNEL_EVENT

fun KotlinTransactionContext.areContacts(userId1: UserId, userId2: UserId): Condition {
    return exists(
        selectFrom(CONTACT)
            .where(CONTACT.USER_ID_1.eq(userId1.value).and(CONTACT.USER_ID_2.eq(userId2.value)))
            .or(CONTACT.USER_ID_2.eq(userId1.value).and(CONTACT.USER_ID_1.eq(userId1.value)))
    ).or(exists(
        selectFrom()
            .where(CHANNEL_EVENT.)
    ))
}
