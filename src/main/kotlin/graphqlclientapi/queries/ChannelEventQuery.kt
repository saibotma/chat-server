package graphqlclientapi.queries

import com.fasterxml.jackson.databind.ObjectMapper
import graphqlclientapi.AuthContext
import graphqlclientapi.ClientApiException
import graphqlclientapi.models.ChannelEventReadPayload
import graphqlclientapi.models.toReadPayload
import graphqlclientapi.resourceNotFound
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

