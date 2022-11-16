package persistence.postgres.queries.userevent

import clientapi.models.UserEventReadPayload
import org.jooq.impl.DSL.value
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.USER_EVENT
import persistence.postgres.mappings.userEventReadPayloadToJson
import persistence.postgres.queries.channelmember.areMembersOfSameChannel

fun KotlinTransactionContext.getUserEvents(userId: String, beforeId: Long, take: Int): List<UserEventReadPayload> {
    return db.select(userEventReadPayloadToJson(userEvent = USER_EVENT))
        .from(USER_EVENT)
        .where(areMembersOfSameChannel(userId1 = USER_EVENT.USER_ID, userId2 = value(userId)))
        .and(USER_EVENT.ID.lt(beforeId))
        .orderBy(USER_EVENT.ID.desc())
        .limit(take)
        .fetchInto(UserEventReadPayload::class.java)
}
