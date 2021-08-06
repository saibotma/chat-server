package clientapi.models

import dev.saibotma.persistence.postgres.jooq.tables.pojos.Message
import models.DetailedUserReadPayload
import java.time.Instant
import java.util.*

interface MessagePayload {
    val text: String?
    val respondedMessageId: UUID?
}

data class MessageWritePayload(override val text: String?, override val respondedMessageId: UUID?) : MessagePayload

data class DetailedMessageReadPayload(
    val id: UUID,
    override val text: String?,
    override val respondedMessageId: UUID?,
    val creator: DetailedUserReadPayload?
) : MessagePayload

fun MessageWritePayload.toMessage(id: UUID, creatorUserId: String, channelId: UUID, createdAt: Instant): Message {
    return Message(
        id = id,
        text = text,
        respondedMessageId = respondedMessageId,
        creatorUserId = creatorUserId,
        channelId = channelId,
        createdAt = createdAt,
    )
}
