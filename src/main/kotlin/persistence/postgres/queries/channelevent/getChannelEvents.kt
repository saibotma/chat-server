package persistence.postgres.queries.channelevent

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.pojos.ChannelEvent
import persistence.jooq.tables.references.CHANNEL
import persistence.jooq.tables.references.CHANNEL_EVENT

fun KotlinTransactionContext.getChannelEvents(beforeId: Long, take: Int): List<ChannelEvent> {
    return db.selectFrom(CHANNEL_EVENT)
        .where(CHANNEL_EVENT.CHANNEL_ID.eq(CHANNEL.ID))
        .and(CHANNEL_EVENT.ID.lt(beforeId))
        .orderBy(CHANNEL_EVENT.ID.desc())
        .limit(take)
        .fetchInto(ChannelEvent::class.java)
}
