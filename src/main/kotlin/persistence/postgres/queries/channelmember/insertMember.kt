package persistence.postgres.queries.channelmember

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.enums.ChannelMemberRole
import persistence.jooq.tables.pojos.ChannelMember
import java.time.Instant
import java.util.*

fun KotlinTransactionContext.insertMember(member: ChannelMember) {
    insertMembers(listOf(member))
}

fun KotlinTransactionContext.insertMember(channelId: UUID, userId: String, role: ChannelMemberRole) {
    insertMember(
        ChannelMember(
            channelId = channelId,
            userId = userId,
            role = role,
            addedAt = Instant.now(),
            updatedAt = null,
        )
    )
}
