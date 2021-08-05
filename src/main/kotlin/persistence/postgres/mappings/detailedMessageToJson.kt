package persistence.postgres.mappings

import clientapi.models.DetailedMessage
import dev.saibotma.persistence.postgres.jooq.tables.User.Companion.USER
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import org.jooq.impl.DSL.select
import persistence.jooq.value
import dev.saibotma.persistence.postgres.jooq.tables.Message as MessageTable

fun detailedMessageToJson(message: MessageTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        DetailedMessage::id.value(message.ID),
        DetailedMessage::text.value(message.TEXT),
        DetailedMessage::respondedMessageId.value(message.RESPONDED_MESSAGE_ID),
        DetailedMessage::extendedMessageId.value(message.EXTENDED_MESSAGE_ID),
        DetailedMessage::creator.value(
            select(detailedUserReadToJson(user = USER))
                .from(USER)
                .where(message.CREATOR_USER_ID.eq(USER.ID))
        )
    )
}
