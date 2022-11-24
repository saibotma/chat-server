package persistence.postgres.queries.channelmember

import models.ChannelMemberWritePayload
import persistence.jooq.KotlinTransactionContext
import util.toOptional
import java.util.*

fun KotlinTransactionContext.updateMembers(channelId: UUID, members: List<ChannelMemberWritePayload>) {
    db.batch(members.map { buildUpdateMemberQuery(channelId = channelId, userId = it.userId, role = it.role.toOptional()) })
        .execute()
}
