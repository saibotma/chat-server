package persistence.postgres.queries

import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelMemberRecord
import persistence.jooq.KotlinTransactionContext
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL_MEMBER
import persistence.postgres.catchPostgresExceptions
import persistence.postgres.isUniqueViolation
import platformapi.models.ChannelMeta
import platformapi.models.ChannelWrite
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
