package persistence.postgres.queries.contact

import clientapi.UserId
import org.jooq.Condition
import org.jooq.Field
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.CONTACT
import persistence.jooq.tables.Contact as ContactTable

fun KotlinTransactionContext.areContacts(userId1: UserId, userId2: UserId): Boolean {
    return db.select(areContacts(userId1 = value(userId1.value), userId2 = value(userId2.value)))
        .fetchOneInto(Boolean::class.java)!!
}

fun areContacts(userId1: Field<String?>, userId2: Field<String?>): Condition {
    return exists(
        selectFrom(CONTACT)
            .where(areContacts(contact = CONTACT, userId1 = userId1, userId2 = userId2))
    )
}

fun areContacts(contact: ContactTable, userId1: Field<String?>, userId2: Field<String?>): Condition {
    return (contact.USER_ID_1.eq(userId1).and(contact.USER_ID_2.eq(userId2)))
        .or(contact.USER_ID_1.eq(userId2).and(contact.USER_ID_2.eq(userId1)))
}
