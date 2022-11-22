package persistence.postgres.queries.contact

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.pojos.Contact
import persistence.jooq.tables.records.ContactRecord

fun KotlinTransactionContext.insertContact(contact: Contact) {
    db.executeInsert(ContactRecord().apply { from(contact) })
}
