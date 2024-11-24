package graphqlclientapi.queries

import graphqlclientapi.AuthContext
import graphqlclientapi.ClientApiException
import graphqlclientapi.models.DetailedMessageReadPayload
import graphqlclientapi.resourceNotFound
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
        val isMemberOfChannel =
            database.transaction { isMemberOfChannel(channelId = channelId, userId = context.userId) }
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
