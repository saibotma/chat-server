package persistence.postgres.queries.channel

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.pojos.Channel
import persistence.jooq.tables.records.ChannelRecord

fun KotlinTransactionContext.insertChannel(channel: Channel) {
    db.executeInsert(ChannelRecord().apply { from(channel) })
}
