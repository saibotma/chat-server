package persistence.postgres.queries

import models.DetailedChannelMemberReadPayload
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.andIf
import persistence.jooq.tables.Channel.Companion.CHANNEL
import persistence.jooq.tables.ChannelMember.Companion.CHANNEL_MEMBER
import persistence.jooq.tables.pojos.Channel
import persistence.jooq.tables.pojos.ChannelMember
import persistence.postgres.mappings.detailedChannelMemberReadToJson
import java.util.*

fun KotlinTransactionContext.getChannel(channelId: UUID): Channel? {
    return db.select()
        .from(CHANNEL)
        .where(CHANNEL.ID.eq(channelId))
        .fetchOneInto(Channel::class.java)
}

fun KotlinTransactionContext.deleteChannel(channelId: UUID) {
    db.deleteFrom(CHANNEL).where(CHANNEL.ID.eq(channelId)).execute()
}

fun KotlinTransactionContext.getDetailedMember(channelId: UUID, userId: String): DetailedChannelMemberReadPayload? {
    return db.select(detailedChannelMemberReadToJson(channelMember = CHANNEL_MEMBER))
        .from(CHANNEL_MEMBER)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .and(CHANNEL_MEMBER.USER_ID.eq(userId))
        .fetchOneInto(DetailedChannelMemberReadPayload::class.java)
}

fun KotlinTransactionContext.getMembersOf(channelId: UUID, userIdFilter: String? = null): List<ChannelMember> {
    return db.select()
        .from(CHANNEL_MEMBER)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .andIf(userIdFilter != null) { CHANNEL_MEMBER.USER_ID.eq(userIdFilter) }
        .fetchInto(ChannelMember::class.java)
}

fun KotlinTransactionContext.deleteMembersOf(channelId: UUID, userIds: List<String>) {
    db.deleteFrom(CHANNEL_MEMBER)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .and(CHANNEL_MEMBER.USER_ID.`in`(userIds))
        .execute()
}

fun KotlinTransactionContext.deleteMember(channelId: UUID, userId: String) {
    db.deleteFrom(CHANNEL_MEMBER)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .and(CHANNEL_MEMBER.USER_ID.eq(userId))
        .execute()
}



