package clientapi.queries

import clientapi.AuthContext
import com.fasterxml.jackson.databind.ObjectMapper
import models.DetailedChannelReadPayload
import models.DetailedChannelReadPayload2
import persistence.jooq.KotlinDslContext
import persistence.jooq.enums.ChannelEventType
import persistence.postgres.queries.channel.getDetailedChannelsOf

class ChannelQuery(
    private val database: KotlinDslContext,
    private val objectMapper: ObjectMapper,
) {
    suspend fun channels(context: AuthContext): List<DetailedChannelReadPayload> {
        return database.transaction {
            getDetailedChannelsOf(context.userId)
        }
    }

    suspend fun channels2(
        context: AuthContext,
        lastEventType: List<ChannelEventType>,
    ): List<DetailedChannelReadPayload2> {
        return database.transaction {
            getDetailedChannelsOf(
                userId = context.userId,
                lastEventType = lastEventType.toSet(),
                objectMapper = objectMapper,
            )
        }
    }
}
