package persistence.postgres.queries.channelmember

import org.jooq.Condition
import org.jooq.Field
import org.jooq.Record1
import org.jooq.SelectConditionStep
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.funAlias
import persistence.jooq.tables.ChannelMember
import java.util.*

fun KotlinTransactionContext.isMemberOfChannel(
    channelId: UUID,
    userId: String,
): Boolean {
    return db.select(
        field(
            isMemberOfChannel(
                channelId = value(channelId),
                userId = value(userId),
            )
        )
    )
        .fetchOneInto(Boolean::class.java) ?: false
}

fun isMemberOfChannel(channelId: Field<UUID?>, userId: Field<String?>): Condition {
    return userId.`in`(selectUserIdsOfChannel(channelId = channelId))
}

private fun selectUserIdsOfChannel(channelId: Field<UUID?>): SelectConditionStep<Record1<String?>> {
    val funName = ::selectUserIdsOfChannel.name
    val channelMemberAlias = ChannelMember.CHANNEL_MEMBER.funAlias(funName)
    return select(channelMemberAlias.USER_ID)
        .from(channelMemberAlias)
        .where(channelMemberAlias.CHANNEL_ID.eq(channelId))
}
