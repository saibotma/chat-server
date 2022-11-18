package clientapi.queries

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.models.ChannelEventReadPayload
import clientapi.models.toReadPayload
import clientapi.resourceNotFound
import com.fasterxml.jackson.databind.ObjectMapper
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.channelevent.getChannelEvents
import persistence.postgres.queries.channelmember.isMemberOfChannel
import java.util.*

class ChannelEventQuery(private val database: KotlinDslContext, private val objectMapper: ObjectMapper) {
    suspend fun channelEvents(
        context: AuthContext,
        channelId: UUID,
        beforeId: Long,
        take: Int,
    ): List<ChannelEventReadPayload> {
        val isMemberOfChannel =
            database.transaction { isMemberOfChannel(channelId = channelId, userId = context.userId) }
        if (!isMemberOfChannel) throw ClientApiException.resourceNotFound()

        val rawEvents = database.transaction { getChannelEvents(beforeId = beforeId, take = take) }
        return rawEvents.map { it.toReadPayload(objectMapper = objectMapper) }
    }
}

