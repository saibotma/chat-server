package clientapi.models

import clientapi.UserId
import models.DetailedUserReadPayload
import persistence.jooq.tables.pojos.Message
import java.time.Instant
import java.util.*

interface MessagePayload {
    val text: String?
    val repliedMessageId: UUID?
}

data class MessageWritePayload(override val text: String?, override val repliedMessageId: UUID?) : MessagePayload

data class DetailedMessageReadPayload(
    val id: UUID,
    override val text: String?,
    override val repliedMessageId: UUID?,
    val creator: DetailedUserReadPayload?,
    val createdAt: Instant,
) : MessagePayload

fun MessageWritePayload.toMessage(id: UUID, creatorUserId: UserId, channelId: UUID, createdAt: Instant): Message {
    return Message(
        id = id,
        text = text,
        repliedMessageId = repliedMessageId,
        creatorUserId = creatorUserId.value,
        channelId = channelId,
        createdAt = createdAt,
    )
}
