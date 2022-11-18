package persistence.postgres.queries.channelevent

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.pojos.ChannelEvent
import persistence.jooq.tables.references.CHANNEL_EVENT

fun KotlinTransactionContext.getChannelEvent(id: Long): ChannelEvent? {
    return db.selectFrom(CHANNEL_EVENT)
        .where(CHANNEL_EVENT.ID.eq(id))
        .fetchOneInto(ChannelEvent::class.java)
}
