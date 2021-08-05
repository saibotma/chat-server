package persistence.postgres.queries

import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Channel
import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelMemberRecord
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelRecord
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL_MEMBER
import models.DetailedChannelReadPayload
import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.DSL.select
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.andIf
import persistence.jooq.funAlias
import persistence.postgres.mappings.detailedChannelMemberReadToJson
import persistence.postgres.mappings.detailedChannelReadToJson
import models.ChannelMemberWritePayload
import models.DetailedChannelMemberReadPayload
import java.time.Instant.now
import java.util.*

fun KotlinTransactionContext.getChannel(channelId: UUID): Channel? {
    return db.select()
        .from(CHANNEL)
        .where(CHANNEL.ID.eq(channelId))
        .fetchOneInto(Channel::class.java)
}

fun KotlinTransactionContext.insertChannel(channel: Channel) {
    db.executeInsert(ChannelRecord().apply { from(channel) })
}

fun KotlinTransactionContext.updateChannel(id: UUID, name: String?, isManaged: Boolean? = null) {
    db.update(CHANNEL)
        .set(CHANNEL.NAME, name)
        .apply {
            if (isManaged != null) {
                set(CHANNEL.IS_MANAGED, isManaged)
            }
        }
        .where(CHANNEL.ID.eq(id))
        .execute()
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

fun KotlinTransactionContext.isAdminOfChannel(channelId: UUID, userId: String): Boolean {
    return db.fetchExists(
        select()
            .from(CHANNEL_MEMBER)
            .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
            .and(CHANNEL_MEMBER.USER_ID.eq(userId))
            .and(CHANNEL_MEMBER.ROLE.eq(ChannelMemberRole.admin))
    )
}

fun KotlinTransactionContext.getMembersOf(channelId: UUID, userIdFilter: String? = null): List<ChannelMember> {
    return db.select()
        .from(CHANNEL_MEMBER)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .andIf(userIdFilter != null) { CHANNEL_MEMBER.USER_ID.eq(userIdFilter) }
        .fetchInto(ChannelMember::class.java)
}

fun KotlinTransactionContext.upsertMember(channelId: UUID, userId: String, role: ChannelMemberRole) {
    db.insertInto(CHANNEL_MEMBER)
        .set(CHANNEL_MEMBER.CHANNEL_ID, channelId)
        .set(CHANNEL_MEMBER.USER_ID, userId)
        .set(CHANNEL_MEMBER.ROLE, role)
        .set(CHANNEL_MEMBER.ADDED_AT, now())
        .onDuplicateKeyUpdate()
        .set(CHANNEL_MEMBER.ROLE, role)
        .execute()
}

fun KotlinTransactionContext.insertMember(member: ChannelMember) {
    insertMembers(listOf(member))
}

fun KotlinTransactionContext.updateMembers(channelId: UUID, members: List<ChannelMemberWritePayload>) {
    db.batch(members.map { buildUpdateMemberQuery(channelId = channelId, userId = it.userId, role = it.role) })
        .execute()
}

fun KotlinTransactionContext.insertMembers(members: List<ChannelMember>) {
    db.batchInsert(members.map { ChannelMemberRecord().apply { from(it) } }).execute()
}

fun KotlinTransactionContext.buildUpdateMemberQuery(
    channelId: UUID,
    userId: String,
    role: ChannelMemberRole
): UpdateConditionStep<ChannelMemberRecord> {
    return db.update(CHANNEL_MEMBER)
        .set(CHANNEL_MEMBER.ROLE, role)
        .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .and(CHANNEL_MEMBER.USER_ID.eq(userId))
}

fun KotlinTransactionContext.updateMember(channelId: UUID, userId: String, role: ChannelMemberRole) {
    buildUpdateMemberQuery(channelId = channelId, userId = userId, role = role).execute()
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

fun KotlinTransactionContext.getChannelsOf(
    userId: String,
    channelIdFilter: UUID? = null
): List<DetailedChannelReadPayload> {
    return selectChannelsOf(userId = userId, channelIdFilter = channelIdFilter)
        .fetchInto(DetailedChannelReadPayload::class.java)
}

fun KotlinTransactionContext.isMemberOfChannel(channelId: UUID, userId: String): Boolean {
    return db.select(DSL.field(isMemberOfChannel(channelId = DSL.value(channelId), userId = userId)))
        .fetchOneInto(Boolean::class.java) ?: false
}

private fun KotlinTransactionContext.selectChannelsOf(
    userId: String,
    channelIdFilter: UUID? = null
): SelectConditionStep<Record1<JSON>> {
    return db.select(detailedChannelReadToJson(channel = CHANNEL))
        .from(CHANNEL)
        .where(isMemberOfChannel(channelId = CHANNEL.ID, userId = userId))
        .andIf(channelIdFilter != null) { CHANNEL.ID.eq(channelIdFilter) }
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
