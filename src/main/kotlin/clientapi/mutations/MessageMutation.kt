package clientapi.mutations

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.models.DetailedMessageReadPayload
import clientapi.models.MessageWritePayload
import clientapi.models.toMessage
import clientapi.resourceNotFound
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import push.PushService
import java.time.Instant.now
import java.util.*
import java.util.UUID.randomUUID

class MessageMutation(private val database: KotlinDslContext, private val pushService: Optional<PushService>) {
    suspend fun sendMessage(
        context: AuthContext,
        channelId: UUID,
        message: MessageWritePayload,
    ): DetailedMessageReadPayload {
        val userId = context.userId
        val result = database.transaction {
            if (!isMemberOfChannel(channelId = channelId, userId = userId)) {
                throw ClientApiException.resourceNotFound()
            }
            val id = randomUUID()
            insertMessage(
                message.toMessage(
                    id = id,
                    creatorUserId = userId,
                    channelId = channelId,
                    createdAt = now(),
                )
            )
            getMessage(id)!!
        }
        if (pushService.isPresent) {
            pushService.get()
                .sendPushNotificationForNewMessage(channelId = channelId, creatorId = userId, message = message)
        }
        return result
    }

    suspend fun editMessage(
        context: AuthContext,
        id: UUID,
        text: String?,
    ): DetailedMessageReadPayload {
        val userId = context.userId
        return database.transaction {
            if (!isCreatorOfMessage(messageId = id, userId = userId)) {
                throw ClientApiException.resourceNotFound()
            }
            updateMessage(messageId = id, text = text)
            getMessage(id)!!
        }
    }

    suspend fun deleteMessage(context: AuthContext, id: UUID): Boolean {
        val userId = context.userId
        database.transaction {
            if (!isCreatorOfMessage(messageId = id, userId = userId)) {
                throw ClientApiException.resourceNotFound()
            }
            deleteMessage(id = id)
        }
        return true
    }
}
