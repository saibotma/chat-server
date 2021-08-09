package persistence.postgres.mappings

import persistence.jooq.tables.User.Companion.USER
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import org.jooq.impl.DSL.select
import persistence.jooq.value
import models.DetailedChannelMemberReadPayload
import persistence.jooq.tables.ChannelMember as ChannelMemberTable

fun detailedChannelMemberReadToJson(channelMember: ChannelMemberTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        DetailedChannelMemberReadPayload::channelId.value(channelMember.CHANNEL_ID),
        DetailedChannelMemberReadPayload::userId.value(channelMember.USER_ID),
        DetailedChannelMemberReadPayload::user.value(
            select(detailedUserReadToJson(USER)).from(USER).where(USER.ID.eq(channelMember.USER_ID))
        ),
        DetailedChannelMemberReadPayload::role.value(channelMember.ROLE),
        DetailedChannelMemberReadPayload::addedAt.value(channelMember.ADDED_AT)
    )
}
