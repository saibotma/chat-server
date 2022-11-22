package persistence.postgres.mappings

import models.DetailedChannelMemberReadPayload2
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import persistence.jooq.value
import persistence.jooq.tables.ChannelMember as ChannelMemberTable

fun detailedChannelMemberRead2ToJson(channelMember: ChannelMemberTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        DetailedChannelMemberReadPayload2::channelId.value(channelMember.CHANNEL_ID),
        DetailedChannelMemberReadPayload2::userId.value(channelMember.USER_ID),
        DetailedChannelMemberReadPayload2::role.value(channelMember.ROLE),
        DetailedChannelMemberReadPayload2::addedAt.value(channelMember.ADDED_AT)
    )
}
