package persistence.postgres.mappings

import clientapi.models.DetailedMessageReadPayload
import persistence.jooq.tables.User.Companion.USER
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import org.jooq.impl.DSL.select
import persistence.jooq.value
import persistence.jooq.tables.Message as MessageTable

fun detailedMessageReadToJson(message: MessageTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        DetailedMessageReadPayload::id.value(message.ID),
        DetailedMessageReadPayload::text.value(message.TEXT),
        DetailedMessageReadPayload::repliedMessageId.value(message.REPLIED_MESSAGE_ID),
        DetailedMessageReadPayload::creator.value(
            select(detailedUserReadToJson(user = USER))
                .from(USER)
                .where(message.CREATOR_USER_ID.eq(USER.ID))
        ),
        DetailedMessageReadPayload::createdAt.value(message.CREATED_AT),
    )
}
