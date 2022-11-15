package persistence.postgres.queries.channel

import models.DetailedChannelReadPayload
import models.DetailedChannelReadPayload2
import org.jooq.JSON
import org.jooq.Record1
import org.jooq.SelectConditionStep
import org.jooq.impl.DSL.select
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.andIf
import persistence.jooq.enums.ChannelEventType
import persistence.jooq.tables.Channel
import persistence.jooq.tables.references.CHANNEL
import persistence.jooq.tables.references.CHANNEL_EVENT
import persistence.postgres.mappings.detailedChannelRead2ToJson
import persistence.postgres.mappings.detailedChannelReadToJson
import persistence.postgres.queries.channelmember.isMemberOfChannel
import java.util.*

fun KotlinTransactionContext.getDetailedChannelsOf(
    userId: String,
    lastEventType: Set<ChannelEventType>
): List<DetailedChannelReadPayload2> {
    return db.select(
        select(CHANNEL_EVENT.ID)
            .from(CHANNEL_EVENT)
            .where(CHANNEL_EVENT.TYPE.`in`(lastEventType))
            .orderBy(CHANNEL_EVENT.ID.desc())
            .limit(1)
            .asField<Long>(),
        detailedChannelRead2ToJson(channel = CHANNEL, lastEventType = lastEventType.toSet())
    )
        .from(CHANNEL)
        .where(isMemberOfChannel(channelId = CHANNEL.ID, userId = userId))
        .orderBy(CHANNEL_EVENT.ID.desc())
        .fetchInto(DetailedChannelReadPayload2::class.java)
}

fun KotlinTransactionContext.getDetailedChannelsOf(
    userId: String,
    channelIdFilter: UUID? = null
): List<DetailedChannelReadPayload> {
    return selectDetailedChannelsOf(userId = userId, channelIdFilter = channelIdFilter)
        .fetchInto(DetailedChannelReadPayload::class.java)
}

private fun KotlinTransactionContext.selectDetailedChannelsOf(
    userId: String,
    channelIdFilter: UUID? = null
): SelectConditionStep<Record1<JSON>> {
    return db.select(detailedChannelReadToJson(channel = Channel.CHANNEL))
        .from(Channel.CHANNEL)
        .where(isMemberOfChannel(channelId = Channel.CHANNEL.ID, userId = userId))
        .andIf(channelIdFilter != null) { Channel.CHANNEL.ID.eq(channelIdFilter) }
}
