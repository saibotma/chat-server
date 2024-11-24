package apis.clientapi.models

import persistence.jooq.enums.ChannelMemberRole
import persistence.jooq.tables.pojos.Channel
import persistence.jooq.tables.pojos.ChannelMember
import java.util.*

data class ChannelCreatePayload(val name: String, val members: List<ChannelMemberWritePayload>)

fun ChannelCreatePayload.toChannel(id: UUID): Channel {
    return Channel(id = id, name = name, isManaged = false)
}

data class ChannelMemberWritePayload(val userId: String, val role: ChannelMemberRole)

fun ChannelMemberWritePayload.toChannelMember(channelId: UUID): ChannelMember {
    return ChannelMember(channelId = channelId, userId = userId, role = role)
}
