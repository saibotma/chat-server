package persistence.postgres.queries.channelevent

import clientapi.UserId
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.CHANNEL_MEMBER
import java.util.*

fun KotlinTransactionContext.getUserIdsForChannelEvent(channelId: UUID): Set<UserId> {
    return db.select(CHANNEL_MEMBER.USER_ID)
        .from(CHANNEL_MEMBER)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .fetchInto(String::class.java)
        .map(::UserId)
        .toSet()
}
