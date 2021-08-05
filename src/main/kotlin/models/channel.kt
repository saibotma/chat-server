package models

import dev.saibotma.persistence.postgres.jooq.tables.pojos.Channel
import java.time.Instant
import java.util.*

interface ChannelPayload {
    val name: String?
    val isManaged: Boolean
}

data class ChannelWritePayload(override val name: String?, override val isManaged: Boolean) : ChannelPayload

data class ChannelReadPayload(
    val id: UUID,
    override val name: String?,
    override val isManaged: Boolean,
    val createdAt: Instant
) : ChannelPayload

data class DetailedChannelReadPayload(
    val id: UUID,
    override val name: String?,
    override val isManaged: Boolean,
    val members: List<DetailedChannelMemberReadPayload>,
    val createdAt: Instant,
) : ChannelPayload

fun ChannelWritePayload.toChannel(id: UUID, createdAt: Instant): Channel {
    return Channel(id = id, name = name, isManaged = isManaged, createdAt = createdAt)
}

fun Channel.toChannelRead(): ChannelReadPayload {
    return ChannelReadPayload(id = id!!, name = name, isManaged = isManaged!!, createdAt = createdAt!!)
}
