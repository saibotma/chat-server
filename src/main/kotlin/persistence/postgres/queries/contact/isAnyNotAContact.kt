package persistence.postgres.queries.contact

import clientapi.UserId
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinTransactionContext

fun KotlinTransactionContext.isAnyNotAContact(userIds: Set<UserId>, of: UserId): Boolean {
    val userIdsTable = name("user_ids")
    val userIdColumn = name("user_id")
    val userIdField = field(userIdColumn, String::class.java)
    return db.fetchExists(
        select(userIdField)
            // https://www.postgresql.org/docs/current/queries-values.html
            .from(values(*userIds.map { row(it.value) }.toTypedArray()).`as`(userIdsTable, userIdColumn))
            .where(not(areContacts(userId1 = value(of.value), userId2 = userIdField)))
    )
}
