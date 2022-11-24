package persistence.postgres.queries.channelmember

import org.jooq.UpdateConditionStep
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.enums.ChannelMemberRole
import persistence.jooq.tables.ChannelMember
import persistence.jooq.tables.records.ChannelMemberRecord
import persistence.jooq.tables.references.CHANNEL_MEMBER
import util.Optional
import java.time.Instant
import java.util.*

fun KotlinTransactionContext.buildUpdateMemberQuery(
    channelId: UUID,
    userId: String,
    role: Optional<ChannelMemberRole>?,
): UpdateConditionStep<ChannelMemberRecord> {
    return db.update(ChannelMember.CHANNEL_MEMBER)
        .apply {
            if (role != null) set(CHANNEL_MEMBER.ROLE, role.value)
        }
        .set(CHANNEL_MEMBER.UPDATED_AT, Instant.now())
        .where(ChannelMember.CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
        .and(ChannelMember.CHANNEL_MEMBER.USER_ID.eq(userId))
}
