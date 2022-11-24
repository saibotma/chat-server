package models

import clientapi.models.ChannelEventReadPayload
import clientapi.models.DetailedMessageReadPayload
import persistence.jooq.tables.pojos.Channel
import java.time.Instant
import java.util.*

interface ChannelPayload {
    val name: String?
    val description: String?
    val isManaged: Boolean
}

data class ChannelWritePayload(
    override val name: String?,
    override val description: String?,
    override val isManaged: Boolean
) : ChannelPayload

interface ChannelReadPayloadInterface : ChannelPayload {
    val id: UUID
    override val name: String?
    override val isManaged: Boolean
    val createdAt: Instant
    val updatedAt: Instant?
}

data class ChannelReadPayload(
    override val id: UUID,
    override val name: String?,
    override val description: String?,
    override val isManaged: Boolean,
    override val createdAt: Instant,
    override val updatedAt: Instant?
) : ChannelReadPayloadInterface

data class DetailedChannelReadPayload(
    override val id: UUID,
    override val name: String?,
    override val description: String?,
    override val isManaged: Boolean,
    val members: List<DetailedChannelMemberReadPayload>,
    val messages: List<DetailedMessageReadPayload>,
    override val createdAt: Instant,
    override val updatedAt: Instant?,
) : ChannelReadPayloadInterface

data class DetailedChannelReadPayload2(
    override val id: UUID,
    /**
     * The name of the channel when it is a group channel.
     */
    override val name: String?,
    override val description: String?,
    override val isManaged: Boolean,
    val members: List<DetailedChannelMemberReadPayload2>,
    /**
     * The last event of the channel of the type that the user requested.
     */
    val lastEvent: ChannelEventReadPayload?,
    override val createdAt: Instant,
    override val updatedAt: Instant?
) : ChannelReadPayloadInterface

fun ChannelWritePayload.toChannel(id: UUID, createdAt: Instant): Channel {
    return Channel(id = id, name = name, isManaged = isManaged, createdAt = createdAt)
}

fun Channel.toChannelRead(): ChannelReadPayload {
    return ChannelReadPayload(
        id = id!!,
        name = name,
        description = description,
        isManaged = isManaged!!,
        createdAt = createdAt!!,
        updatedAt = updatedAt,
    )
}
