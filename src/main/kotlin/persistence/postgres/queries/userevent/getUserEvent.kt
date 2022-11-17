package persistence.postgres.queries.userevent

import clientapi.models.UserEventReadPayload
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.USER_EVENT
import persistence.postgres.mappings.userEventReadPayloadToJson

fun KotlinTransactionContext.getUserEvent(id: Long): UserEventReadPayload? {
    return db.select(userEventReadPayloadToJson(userEvent = USER_EVENT))
        .from(USER_EVENT)
        .where(USER_EVENT.ID.eq(id))
        .fetchOneInto(UserEventReadPayload::class.java)
}
