package clientapi.queries

import clientapi.AuthContext
import clientapi.models.DetailedChannel
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getChannelsOf

class ChannelQuery(
    private val database: KotlinDslContext,
) {
    suspend fun channels(context: AuthContext): List<DetailedChannel> {
        return database.transaction {
            getChannelsOf(context.userId)
        }
    }
}
