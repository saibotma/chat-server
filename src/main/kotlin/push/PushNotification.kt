package push

import java.util.*


sealed class PushNotification(
    val title: String,
    val message: String,
    val data: Map<String, Any>,
) {
    sealed class Channel(creatorName: String, channelName: String?, message: String, channelId: UUID) :
        PushNotification(
            title = "$creatorName${if (channelName != null) " @ $channelName" else ""}",
            message = message,
            data = mapOf("type" to "channel", "channelId" to channelId)
        ) {
        data class NewMessage(
            val creatorName: String,
            val channelName: String?,
            val text: String,
            val channelId: UUID
        ) :
            Channel(
                creatorName = creatorName,
                channelName = channelName,
                message = text,
                channelId = channelId
            )
    }
}
