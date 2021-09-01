package push

import clientapi.models.MessageWritePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.kotlin.logger
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import java.util.*

class PushService(
    private val dslContext: KotlinDslContext,
    private val pushNotificationSender: PushNotificationSender,
    // Required for testing
    private val sendNotificationsBlocking: Boolean = false,
) {
    private val logger = logger()

    suspend fun sendPushNotificationForNewMessage(channelId: UUID, creatorId: String, message: MessageWritePayload) {
        val affectedUserIds = dslContext.transaction { getUserIdsOfChannel(channelId = channelId) }
        val creator = dslContext.transaction { getUser(creatorId)!! }
        val channel = dslContext.transaction { getChannel(channelId)!! }

        sendPushNotification(
            userIds = affectedUserIds,
            pushNotification = PushNotification.Channel.NewMessage(
                creatorName = creator.name ?: "",
                channelName = channel.name,
                text = message.text ?: "",
                channelId = channelId
            ),
            ownUserId = creatorId,
        )
    }

    // region helper

    private suspend fun sendPushNotification(
        userIds: List<String>,
        pushNotification: PushNotification,
        ownUserId: String? = null,
    ) {

        val pushTokens =
            dslContext.transaction { getPushTokenOfUsers(*userIds.filter { it != ownUserId }.toTypedArray()) }

        val unknownTokens = mutableListOf<String>()
        if (sendNotificationsBlocking) {
            for (token in pushTokens) {
                val isUnknown = !pushNotificationSender.send(token, pushNotification)
                if (isUnknown) unknownTokens.add(token.pushToken!!)
            }
        } else {
            withContext(Dispatchers.IO) {
                for (token in pushTokens) {
                    launch {
                        logger.info("Sending push notification $pushNotification to token $token.")
                        val isUnknown = !pushNotificationSender.send(token, pushNotification)
                        if (isUnknown) unknownTokens.add(token.pushToken!!)
                    }
                }
            }
        }

        if (unknownTokens.isNotEmpty()) dslContext.transaction { deleteTokens(unknownTokens) }
    }

    // endregion
}
