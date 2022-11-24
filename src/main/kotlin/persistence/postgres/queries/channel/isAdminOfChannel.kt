package persistence.postgres.queries.channel

import clientapi.UserId
import org.jooq.impl.DSL
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.enums.ChannelMemberRole
import persistence.jooq.tables.ChannelMember
import java.util.*

fun KotlinTransactionContext.isAdminOfChannel(channelId: UUID, userId: UserId): Boolean {
    return db.fetchExists(
        DSL.select()
            .from(ChannelMember.CHANNEL_MEMBER)
            .where(ChannelMember.CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
            .and(ChannelMember.CHANNEL_MEMBER.USER_ID.eq(userId.value))
            .and(ChannelMember.CHANNEL_MEMBER.ROLE.eq(ChannelMemberRole.admin))
    )
}
