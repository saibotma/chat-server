package clientapi.queries

import clientapi.AuthContext
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.ChannelEvent
import java.util.*

class ChannelEventQuery(private val database: KotlinDslContext) {
    suspend fun messages2(
        context: AuthContext,
        channelId: UUID,
        byEventId: Long,
        take: Int,
    ): List<ChannelEventReadPayload> {

        TODO()
    }
}

data class ChannelEventReadPayload(val event: ChannelEvent)
