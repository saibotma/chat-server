package persistence.postgres.queries.channelmember

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.pojos.ChannelMember
import persistence.jooq.tables.records.ChannelMemberRecord

fun KotlinTransactionContext.insertMembers(members: List<ChannelMember>) {
    db.batchInsert(members.map { ChannelMemberRecord().apply { from(it) } }).execute()
}
