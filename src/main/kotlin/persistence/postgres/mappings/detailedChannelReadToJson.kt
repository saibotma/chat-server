package persistence.postgres.mappings

import persistence.jooq.tables.references.CHANNEL_MEMBER
import models.DetailedChannelReadPayload
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.*
import persistence.jooq.jsonArrayAggNoNull
import persistence.jooq.value
import persistence.postgres.queries.selectMessagesOf
import persistence.jooq.tables.Channel as ChannelTable

fun detailedChannelReadToJson(channel: ChannelTable): JSONObjectNullStep<JSON> {
    val messageFieldName = name("message")
    val messageField = field(messageFieldName)
    return jsonObject(
        DetailedChannelReadPayload::id.value(channel.ID),
        DetailedChannelReadPayload::name.value(channel.NAME),
        DetailedChannelReadPayload::isManaged.value(channel.IS_MANAGED),
        DetailedChannelReadPayload::members.value(
            select(jsonArrayAggNoNull(detailedChannelMemberReadToJson(CHANNEL_MEMBER)))
                .from(CHANNEL_MEMBER)
                .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channel.ID))
        ),
        DetailedChannelReadPayload::messages.value(
            select(jsonArrayAggNoNull(messageField))
                .from(
                    selectMessagesOf(
                        channelId = channel.ID,
                        byDateTime = null,
                        byMessageId = null,
                        previousLimit = 50,
                        nextLimit = 0,
                        aliasName = messageFieldName,
                    )
                )
        ),
        DetailedChannelReadPayload::createdAt.value(channel.CREATED_AT),
    )
}
