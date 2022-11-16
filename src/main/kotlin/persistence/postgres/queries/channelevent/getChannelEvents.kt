package persistence.postgres.queries.channelevent

import clientapi.models.ChannelEventReadPayload
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.CHANNEL
import persistence.jooq.tables.references.CHANNEL_EVENT
import persistence.postgres.mappings.channelEventReadPayloadToJson

fun KotlinTransactionContext.getChannelEvents(beforeId: Long, take: Int): List<ChannelEventReadPayload> {
    return db.select(channelEventReadPayloadToJson(channelEvent = CHANNEL_EVENT))
        .from(CHANNEL_EVENT)
        .where(CHANNEL_EVENT.CHANNEL_ID.eq(CHANNEL.ID))
        .and(CHANNEL_EVENT.ID.lt(beforeId))
        .orderBy(CHANNEL_EVENT.ID.desc())
        .limit(take)
        .fetchInto(ChannelEventReadPayload::class.java)
}
