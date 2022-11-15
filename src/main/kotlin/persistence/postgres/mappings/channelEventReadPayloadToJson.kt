package persistence.postgres.mappings

import clientapi.models.ChannelEventReadPayload
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import persistence.jooq.value
import persistence.jooq.tables.ChannelEvent as ChannelEventTable

fun channelEventReadPayloadToJson(channelEvent: ChannelEventTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        ChannelEventReadPayload::id.value(channelEvent.ID),
        ChannelEventReadPayload::channelId.value(channelEvent.CHANNEL_ID),
        ChannelEventReadPayload::type.value(channelEvent.TYPE),
        ChannelEventReadPayload::data.value(channelEvent.DATA),
        ChannelEventReadPayload::createdAt.value(channelEvent.CREATED_AT)
    )
}
