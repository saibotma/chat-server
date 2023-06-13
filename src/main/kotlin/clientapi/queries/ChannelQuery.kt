package clientapi.queries

import clientapi.AuthContext
import models.DetailedChannelReadPayload
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getChannelsOf
import java.util.*

class ChannelQuery(
    private val database: KotlinDslContext,
) {
    suspend fun channels(context: AuthContext): List<DetailedChannelReadPayload> {
        return database.transaction {
            getChannelsOf(context.userId)
        }
    }

    suspend fun channel(context: AuthContext, id: UUID): DetailedChannelReadPayload? {
        return database.transaction {
            getChannelsOf(context.userId, channelIdFilter = id)
        }.firstOrNull()
    }
}
