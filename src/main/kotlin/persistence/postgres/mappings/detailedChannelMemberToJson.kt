package persistence.postgres.mappings

import dev.saibotma.persistence.postgres.jooq.tables.User.Companion.USER
import models.DetailedChannelMember
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import org.jooq.impl.DSL.select
import persistence.jooq.value
import dev.saibotma.persistence.postgres.jooq.tables.ChannelMember as ChannelMemberTable

fun detailedChannelMemberToJson(channelMember: ChannelMemberTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        DetailedChannelMember::channelId.value(channelMember.CHANNEL_ID),
        DetailedChannelMember::user.value(select(userToJson(USER)).from(USER).where(USER.ID.eq(channelMember.USER_ID))),
        DetailedChannelMember::role.value(channelMember.ROLE),
        DetailedChannelMember::addedAt.value(channelMember.ADDED_AT)
    )
}
