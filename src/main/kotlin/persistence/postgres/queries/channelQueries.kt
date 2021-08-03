package persistence.postgres.queries

import clientapi.models.DetailedChannel
import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelMemberRecord
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

sealed class InsertChannelError {
    object Duplicate : InsertChannelError()
}

fun KotlinTransactionContext.insertChannel(channel: ChannelMeta): Fallible<InsertChannelError, Unit> {
    return catchPostgresExceptions {
        db.insertInto(CHANNEL)
            .set(CHANNEL.ID, channel.id)
            .set(CHANNEL.NAME, channel.name)
            .set(CHANNEL.IS_MANAGED, channel.isManaged)
            .execute()
        Unit
    }.onFailure {
        when {
            isUniqueViolation(CHANNEL.primaryKey.name) -> InsertChannelError.Duplicate
            else -> throw this
        }
    }

}

fun KotlinTransactionContext.updateChannel(meta: ChannelMeta) {
    db.update(CHANNEL)
        .set(CHANNEL.ID, meta.id)
        .set(CHANNEL.NAME, meta.name)
        .set(CHANNEL.IS_MANAGED, meta.isManaged)
        .execute()
}

fun KotlinTransactionContext.deleteChannel(channelId: UUID) {
    db.deleteFrom(CHANNEL).where(CHANNEL.ID.eq(channelId)).execute()
}

fun KotlinTransactionContext.insertMembers(members: List<ChannelMember>) {
    db.batchInsert(members.map { ChannelMemberRecord().apply { from(it) } }).execute()
}

fun KotlinTransactionContext.deleteMembersOf(channelId: UUID) {
    db.deleteFrom(CHANNEL_MEMBER).where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId)).execute()
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
