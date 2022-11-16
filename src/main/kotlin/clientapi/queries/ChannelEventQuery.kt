package clientapi.queries

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.models.ChannelEventReadPayload
import clientapi.resourceNotFound
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.channelevent.getChannelEvents
import persistence.postgres.queries.channelmember.isMemberOfChannel
import java.util.*

class ChannelEventQuery(private val database: KotlinDslContext) {
    suspend fun channelEvents(
        context: AuthContext,
        channelId: UUID,
        beforeId: Long,
        take: Int,
    ): List<ChannelEventReadPayload> {
        val isMemberOfChannel =
            database.transaction { isMemberOfChannel(channelId = channelId, userId = context.userId) }
        if (!isMemberOfChannel) throw ClientApiException.resourceNotFound()

        return database.transaction { getChannelEvents(beforeId = beforeId, take = take) }
    }
}
