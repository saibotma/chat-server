package persistence.postgres.mappings

import clientapi.models.UserEventReadPayload
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import persistence.jooq.value
import persistence.jooq.tables.UserEvent as UserEventTable

fun userEventReadPayloadToJson(userEvent: UserEventTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        UserEventReadPayload::id.value(userEvent.ID),
        UserEventReadPayload::userId.value(userEvent.USER_ID),
        UserEventReadPayload::type.value(userEvent.TYPE),
        UserEventReadPayload::data.value(userEvent.DATA),
        UserEventReadPayload::createdAt.value(userEvent.CREATED_AT)
    )
}
