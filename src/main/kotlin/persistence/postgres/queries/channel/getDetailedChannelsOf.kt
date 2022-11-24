package persistence.postgres.queries.channel

import clientapi.UserId
import clientapi.models.toReadPayload
import com.fasterxml.jackson.databind.ObjectMapper
import models.DetailedChannelMemberReadPayload2
import models.DetailedChannelReadPayload
import models.DetailedChannelReadPayload2
import org.jooq.JSON
import org.jooq.Record1
import org.jooq.SelectConditionStep
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.andIf
import persistence.jooq.enums.ChannelEventType
import persistence.jooq.tables.pojos.ChannelEvent
import persistence.jooq.tables.references.CHANNEL
import persistence.jooq.tables.references.CHANNEL_EVENT
import persistence.jooq.tables.references.CHANNEL_MEMBER
import persistence.postgres.mappings.detailedChannelMemberRead2ToJson
import persistence.postgres.mappings.detailedChannelReadToJson
import persistence.postgres.queries.channelmember.isMemberOfChannel
import java.util.*

fun KotlinTransactionContext.getDetailedChannelsOf(
    userId: UserId,
    lastEventType: Set<ChannelEventType>,
    objectMapper: ObjectMapper,
): List<DetailedChannelReadPayload2> {
    val innerChannelEvent = CHANNEL_EVENT.`as`("inner_channel_event")
    val result = db.select(
        select(CHANNEL_EVENT.ID)
            .from(CHANNEL_EVENT)
            .where(CHANNEL_EVENT.TYPE.`in`(lastEventType))
            .orderBy(CHANNEL_EVENT.ID.desc())
            .limit(1)
            .asField<Long>(),
        CHANNEL.ID,
        CHANNEL.NAME,
        CHANNEL.DESCRIPTION,
        CHANNEL.IS_MANAGED,
        multiset(
            select(detailedChannelMemberRead2ToJson(channelMember = CHANNEL_MEMBER))
                .from(CHANNEL_MEMBER)
                .where(CHANNEL_MEMBER.CHANNEL_ID.eq(CHANNEL.ID))
        ).convertFrom { it.into(DetailedChannelMemberReadPayload2::class.java) },
        row(
            selectFrom(innerChannelEvent)
                .where(innerChannelEvent.CHANNEL_ID.eq(CHANNEL.ID))
                .and(innerChannelEvent.TYPE.`in`(lastEventType))
                .orderBy(innerChannelEvent.ID.desc())
                .limit(1)
        ).convertFrom { it.into(ChannelEvent::class.java) },
        CHANNEL.CREATED_AT,
        CHANNEL.UPDATED_AT,
    )
        .from(CHANNEL)
        .where(isMemberOfChannel(channelId = CHANNEL.ID, userId = value(userId.value)))
        .orderBy(CHANNEL_EVENT.ID.desc())
        .fetch()

    return result.map {
        DetailedChannelReadPayload2(
            id = it.component2()!!,
            name = it.component3(),
            description = it.component4(),
            isManaged = it.component5()!!,
            members = it.component6()!!,
            lastEvent = it.component7()?.toReadPayload(objectMapper = objectMapper),
            createdAt = it.component8()!!,
            updatedAt = it.component9(),
        )
    }
}

fun KotlinTransactionContext.getDetailedChannelsOf(
    userId: UserId,
    channelIdFilter: UUID? = null
): List<DetailedChannelReadPayload> {
    return selectDetailedChannelsOf(userId = userId, channelIdFilter = channelIdFilter)
        .fetchInto(DetailedChannelReadPayload::class.java)
}

private fun KotlinTransactionContext.selectDetailedChannelsOf(
    userId: UserId,
    channelIdFilter: UUID? = null
): SelectConditionStep<Record1<JSON>> {
    return db.select(detailedChannelReadToJson(channel = CHANNEL))
        .from(CHANNEL)
        .where(isMemberOfChannel(channelId = CHANNEL.ID, userId = value(userId.value)))
        .andIf(channelIdFilter != null) { CHANNEL.ID.eq(channelIdFilter) }
}
