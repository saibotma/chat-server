package clientapi.queries

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.models.DetailedMessageReadPayload
import clientapi.resourceNotFound
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getMessagesOf
import persistence.postgres.queries.isMemberOfChannel
import java.time.Instant
import java.util.*

class MessageQuery(private val database: KotlinDslContext) {
    suspend fun messages(
        context: AuthContext,
        channelId: UUID,
        byDateTime: Instant?,
        byMessageId: UUID?,
        previousLimit: Int = 15,
        nextLimit: Int = 15,
    ): List<DetailedMessageReadPayload> {
        // TODO(saibotma): https://github.com/saibotma/chat-server/issues/5
        val userId = context.userId
        return database.transaction {
            if (!isMemberOfChannel(channelId = channelId, userId = userId)) throw ClientApiException.resourceNotFound()
            getMessagesOf(
                channelId = channelId,
                byDateTime = byDateTime,
                byMessageId = byMessageId,
                previousLimit = previousLimit,
                nextLimit = nextLimit
            )
        }
    }
}
