package persistence.postgres.queries.channelevent

import clientapi.models.ChannelEventReadPayload
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.CHANNEL_EVENT
import persistence.postgres.mappings.channelEventReadPayloadToJson

fun KotlinTransactionContext.getChannelEvent(id: Long): ChannelEventReadPayload? {
    return db.select(channelEventReadPayloadToJson(channelEvent = CHANNEL_EVENT))
        .from(CHANNEL_EVENT)
        .where(CHANNEL_EVENT.ID.eq(id))
        .fetchOneInto(ChannelEventReadPayload::class.java)
}
