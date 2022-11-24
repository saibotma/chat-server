package persistence.postgres.queries.channel

import clientapi.UserId
import org.jooq.impl.DSL.selectFrom
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.enums.ChannelMemberRole
import persistence.jooq.tables.references.CHANNEL_MEMBER
import java.util.*

fun KotlinTransactionContext.hasAnotherAdmin(channelId: UUID, adminId: UserId): Boolean {
    return db.fetchExists(
        selectFrom(CHANNEL_MEMBER)
            .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
            .and(CHANNEL_MEMBER.USER_ID.ne(adminId.value))
            .and(CHANNEL_MEMBER.ROLE.eq(ChannelMemberRole.admin))
    )
}
