package persistence.postgres.queries.userevent

import clientapi.UserId
import org.jooq.impl.DSL
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.USER
import persistence.postgres.queries.channelmember.areMembersOfSameChannel

fun KotlinTransactionContext.getUserIdsForUserEvent(userEventUserId: String): Set<UserId> {
    return db.select(USER.ID)
        .from(USER)
        .where(areMembersOfSameChannel(userId1 = USER.ID, userId2 = DSL.value(userEventUserId)))
        .fetchInto(String::class.java)
        .map(::UserId)
        .toSet()
}
