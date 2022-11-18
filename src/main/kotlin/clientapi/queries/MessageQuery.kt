package clientapi.queries

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.models.DetailedMessageReadPayload
import clientapi.resourceNotFound
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.channelmember.isMemberOfChannel
import persistence.postgres.queries.getMessage
import persistence.postgres.queries.getMessagesOf
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
        val isMemberOfChannel = database.transaction { isMemberOfChannel(channelId = channelId, userId = userId) }
        if (!isMemberOfChannel) throw ClientApiException.resourceNotFound()

        return database.transaction {
            getMessagesOf(
                channelId = channelId,
                byDateTime = byDateTime,
                byMessageId = byMessageId,
                previousLimit = previousLimit,
                nextLimit = nextLimit
            )
        }
    }

    suspend fun message(context: AuthContext, channelId: UUID, messageId: UUID): DetailedMessageReadPayload {
        val isMemberOfChannel =
            database.transaction { isMemberOfChannel(channelId = channelId, userId = context.userId) }
        if (!isMemberOfChannel) throw ClientApiException.resourceNotFound()

        return database.transaction { getMessage(messageId) } ?: throw ClientApiException.resourceNotFound()
    }
}
