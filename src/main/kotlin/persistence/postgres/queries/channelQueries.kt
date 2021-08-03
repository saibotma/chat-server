package persistence.postgres.queries

import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelMemberRecord
import persistence.jooq.KotlinTransactionContext
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL_MEMBER
import org.jooq.impl.DSL.exists
import org.jooq.impl.DSL.select
import persistence.postgres.catchPostgresExceptions
import persistence.postgres.isUniqueViolation
import persistence.postgres.mappings.detailedChannelToJson
import models.ChannelMeta
import models.DetailedChannel
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
    return db.select(detailedChannelToJson(channel = CHANNEL))
        .from(CHANNEL)
        .where(exists(select().from(CHANNEL_MEMBER).where(CHANNEL_MEMBER.USER_ID.eq(userId))))
        .fetchInto(DetailedChannel::class.java)
}

fun KotlinTransactionContext.getChannelsOf(accountMemberId: UUID): List<clientapi.models.DetailedChannel> {
    return selectChannelsOf(accountMemberId)
        .fetchInto(clientapi.models.DetailedChannel::class.java)
}
