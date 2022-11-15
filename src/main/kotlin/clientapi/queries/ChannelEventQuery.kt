package clientapi.queries

import clientapi.AuthContext
import persistence.jooq.KotlinDslContext
import persistence.jooq.tables.references.CHANNEL
import persistence.jooq.tables.references.CHANNEL_EVENT
import persistence.postgres.queries.channelmember.isMemberOfChannel
import java.util.*

class ChannelEventQuery(private val database: KotlinDslContext) {
    suspend fun events(
        context: AuthContext,
        channelId: UUID,
        byEventId: Long,
        take: Int,
    ): List<ChannelEventReadPayload> {
        database.transaction {
            if (!isMemberOfChannel(channelId = channelId, userId = context.userId)) {
                TODO()
            }
        }

        return database.transaction {
            db.select(channelEventReadPayloadToJson(channelEvent = CHANNEL_EVENT))
                .from(CHANNEL_EVENT)
                .where(CHANNEL_EVENT.CHANNEL_ID.eq(CHANNEL.ID))
                .and(CHANNEL_EVENT.ID.lt(byEventId))
                .orderBy(CHANNEL_EVENT.ID.desc())
                .limit(take)
                .fetchInto(ChannelEventReadPayload::class.java)
        }
    }
}
