package clientapi.models

import persistence.jooq.enums.ChannelEventType
import java.time.Instant
import java.util.*

data class ChannelEventReadPayload(
    val id: Long,
    val channelId: UUID,
    val type: ChannelEventType,
    val data: Map<String, Any?>,
    val createdAt: Instant,
)
