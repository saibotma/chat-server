package platformapi.models

import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelMemberRecord
import java.time.Instant
import java.util.*

interface ChannelMemberPayload {
    val userId: String
    val role: ChannelMemberRole
}

data class ChannelMemberWritePayload(override val userId: String, override val role: ChannelMemberRole) :
    ChannelMemberPayload

fun ChannelMemberWritePayload.toChannelMember(channelId: UUID, addedAt: Instant): ChannelMember {
    return ChannelMember(channelId = channelId, userId = userId, role = role, addedAt = addedAt)
}
