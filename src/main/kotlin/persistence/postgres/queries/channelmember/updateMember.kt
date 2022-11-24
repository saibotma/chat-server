package persistence.postgres.queries.channelmember

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.enums.ChannelMemberRole
import util.Optional
import java.util.*

fun KotlinTransactionContext.updateMember(channelId: UUID, userId: String, role: Optional<ChannelMemberRole>?) {
    buildUpdateMemberQuery(channelId = channelId, userId = userId, role = role).execute()
}
