package platformapi.models

import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelMemberRecord
import java.time.Instant
import java.util.*

interface ChannelMemberPayload {
    val userId: String
    val role: ChannelMemberRole
}

data class ChannelMemberWritePayload(override val userId: String, override val role: ChannelMemberRole) :
    ChannelMemberPayload

data class ChannelMemberReadPayload(
    override val userId: String,
    override val role: ChannelMemberRole,
    val addedAt: Instant
) : ChannelMemberPayload

fun ChannelMemberWritePayload.toChannelMemberRecord(channelId: UUID, addedAt: Instant): ChannelMemberRecord {
    return ChannelMemberRecord(channelId = channelId, userId = userId, role = role, addedAt = addedAt)
}
