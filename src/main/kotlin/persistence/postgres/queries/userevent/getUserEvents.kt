package persistence.postgres.queries.userevent

import org.jooq.impl.DSL.value
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.pojos.UserEvent
import persistence.jooq.tables.references.USER_EVENT
import persistence.postgres.queries.channelmember.areMembersOfSameChannel

fun KotlinTransactionContext.getUserEvents(userId: String, beforeId: Long, take: Int): List<UserEvent> {
    return db.selectFrom(USER_EVENT)
        .where(areMembersOfSameChannel(userId1 = USER_EVENT.USER_ID, userId2 = value(userId)))
        .and(USER_EVENT.ID.lt(beforeId))
        .orderBy(USER_EVENT.ID.desc())
        .limit(take)
        .fetchInto(UserEvent::class.java)
}
