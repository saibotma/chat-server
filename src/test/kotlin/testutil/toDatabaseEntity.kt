package testutil

import clientapi.models.CreateChannelInputMember
import models.ChannelMemberWritePayload
import models.ChannelReadPayload
import persistence.jooq.tables.pojos.Channel
import persistence.jooq.tables.pojos.ChannelMember
import java.util.*

fun ChannelReadPayload.toChannel(): Channel {
    return Channel(
        id = id,
        name = name,
        isManaged = isManaged,
        createdAt = createdAt,
        description = null,
        updatedAt = updatedAt,
        creatorUserId = null,
    )
}

fun ChannelMemberWritePayload.toChannelMember(channelId: UUID): ChannelMember {
    return ChannelMember(
        channelId = channelId,
        userId = userId,
        role = role,
    )
}

fun CreateChannelInputMember.toChannelMember(channelId: UUID): ChannelMember {
    return ChannelMember(
        channelId = channelId,
        userId = userId,
        role = role,
    )
}
