package clientapi.mutations

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.resourceNotFound
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Message
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import java.time.Instant.now
import java.util.*
import java.util.UUID.randomUUID

class MessageMutation(private val database: KotlinDslContext) {
    suspend fun sendMessage(
        context: AuthContext,
        channelId: UUID,
        message: String,
        respondedMessageId: UUID? = null,
        extendedMessageId: UUID? = null,
    ) {
        val userId = context.userId
        database.transaction {
            if (!isMemberOfChannel(channelId = channelId, userId = userId)) {
                throw ClientApiException.resourceNotFound()
            }
            insertMessage(
                Message(
                    id = randomUUID(),
                    text = message,
                    respondedMessageId = respondedMessageId,
                    extendedMessageId = extendedMessageId,
                    creatorUserId = userId,
                    channelId = channelId,
                    createdAt = now(),
                )
            )
        }
    }

    suspend fun editMessage(
        context: AuthContext,
        messageId: UUID,
        message: String,
        respondedMessageId: UUID? = null,
        extendedMessageId: UUID? = null,
    ) {
        val userId = context.userId
        database.transaction {
            if (!isCreatorOfMessage(messageId = messageId, userId = userId)) {
                throw ClientApiException.resourceNotFound()
            }
            updateMessage(
                messageId = messageId,
                text = message,
                respondedMessageId = respondedMessageId,
                extendedMessageId = extendedMessageId,
            )
        }
    }

    suspend fun deleteMessage(context: AuthContext, id: UUID) {
        val userId = context.userId
        database.transaction {
            if (!isCreatorOfMessage(messageId = id, userId = userId)) {
                throw ClientApiException.resourceNotFound()
            }
            deleteMessage(id = id)
        }
    }
}
