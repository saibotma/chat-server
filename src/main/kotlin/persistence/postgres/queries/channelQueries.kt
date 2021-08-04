package persistence.postgres.queries

import clientapi.models.DetailedChannel
import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelMemberRecord
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelRecord
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL_MEMBER
import models.ChannelMeta
import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.DSL.select
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.andIf
import persistence.jooq.funAlias
import persistence.postgres.catchPostgresExceptions
import persistence.postgres.isUniqueViolation
import persistence.postgres.mappings.detailedChannelToJson
import util.Fallible
import java.util.*

fun KotlinTransactionContext.insertChannel(channel: ChannelRecord) = db.executeInsert(channel)

fun KotlinTransactionContext.updateChannel(id: UUID, name: String?, isManaged: Boolean) {
    db.update(CHANNEL)
        .set(CHANNEL.NAME, name)
        .set(CHANNEL.IS_MANAGED, isManaged)
        .where(CHANNEL.ID.eq(id))
        .execute()
}

fun KotlinTransactionContext.deleteChannel(channelId: UUID) {
    db.deleteFrom(CHANNEL).where(CHANNEL.ID.eq(channelId)).execute()
}

fun KotlinTransactionContext.getMembersOf(channelId: UUID): List<ChannelMember> {
    return db.select()
        .from(CHANNEL_MEMBER)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .fetchInto(ChannelMember::class.java)
}

fun KotlinTransactionContext.insertMembers(members: List<ChannelMemberRecord>) {
    db.batchInsert(members).execute()
}

fun KotlinTransactionContext.deleteMembersOf(channelId: UUID, userIds: List<String>) {
    db.deleteFrom(CHANNEL_MEMBER)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .and(CHANNEL_MEMBER.USER_ID.`in`(userIds)).execute()
}

fun KotlinTransactionContext.deleteChannelMember(channelId: UUID, userId: String) {
    db.deleteFrom(CHANNEL_MEMBER)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .and(CHANNEL_MEMBER.USER_ID.eq(userId))
        .execute()
}

fun KotlinTransactionContext.getChannelsOf(userId: String): List<DetailedChannel> {
    return selectChannelsOf(userId)
        .fetchInto(DetailedChannel::class.java)
}

fun KotlinTransactionContext.isMemberOfChannel(channelId: UUID, userId: String): Boolean {
    return db.select(DSL.field(isMemberOfChannel(channelId = DSL.value(channelId), userId = userId)))
        .fetchOneInto(Boolean::class.java) ?: false
}

private fun KotlinTransactionContext.selectChannelsOf(
    userId: String,
    chatRoomIdFilter: UUID? = null
): SelectConditionStep<Record1<JSON>> {
    return db.select(detailedChannelToJson(channel = CHANNEL))
        .from(CHANNEL)
        .where(isMemberOfChannel(channelId = CHANNEL.ID, userId = userId))
        .andIf(chatRoomIdFilter != null) { CHANNEL.ID.eq(chatRoomIdFilter) }
}

fun isMemberOfChannel(channelId: Field<UUID?>, userId: String): Condition {
    return DSL.value(userId).`in`(selectUserIdsOfChannel(channelId = channelId))
}

private fun selectUserIdsOfChannel(channelId: Field<UUID?>): SelectConditionStep<Record1<String?>> {
    val funName = ::selectUserIdsOfChannel.name
    val channelMemberAlias = CHANNEL_MEMBER.funAlias(funName)
    return select(channelMemberAlias.USER_ID)
        .from(channelMemberAlias)
        .where(channelMemberAlias.CHANNEL_ID.eq(channelId))
}
