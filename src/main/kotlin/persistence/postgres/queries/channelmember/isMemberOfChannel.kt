package persistence.postgres.queries.channelmember

import org.jooq.Condition
import org.jooq.Field
import org.jooq.Record1
import org.jooq.SelectConditionStep
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.enums.ChannelEventType
import persistence.jooq.funAlias
import persistence.jooq.tables.ChannelMember
import persistence.jooq.tables.references.CHANNEL_EVENT
import java.util.*

fun KotlinTransactionContext.isMemberOfChannel(
    channelId: UUID,
    userId: String,
    shouldIncludeAncient: Boolean
): Boolean {
    return db.select(
        field(
            isMemberOfChannel(
                channelId = value(channelId),
                userId = value(userId),
                shouldIncludeAncient = shouldIncludeAncient,
            )
        )
    )
        .fetchOneInto(Boolean::class.java) ?: false
}

fun isMemberOfChannel(channelId: Field<UUID?>, userId: Field<String?>, shouldIncludeAncient: Boolean): Condition {
    val funName = ::isMemberOfChannel.name
    val innerChannelEvent = CHANNEL_EVENT.`as`(funName)

    if (shouldIncludeAncient) {
        return exists(
            selectFrom(innerChannelEvent)
                .where(innerChannelEvent.CHANNEL_ID.eq(channelId))
                .and(innerChannelEvent.TYPE.eq(ChannelEventType.add_member))
                .and(innerChannelEvent.DATA.contains(jsonbObject(jsonEntry("user_id", userId))))
        )
    }
    return userId.`in`(selectUserIdsOfChannel(channelId = channelId))
}

private fun selectUserIdsOfChannel(channelId: Field<UUID?>): SelectConditionStep<Record1<String?>> {
    val funName = ::selectUserIdsOfChannel.name
    val channelMemberAlias = ChannelMember.CHANNEL_MEMBER.funAlias(funName)
    return DSL.select(channelMemberAlias.USER_ID)
        .from(channelMemberAlias)
        .where(channelMemberAlias.CHANNEL_ID.eq(channelId))
}
