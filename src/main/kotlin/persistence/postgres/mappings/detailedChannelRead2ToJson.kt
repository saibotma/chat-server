package persistence.postgres.mappings

import models.DetailedChannelReadPayload2
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import org.jooq.impl.DSL.select
import persistence.jooq.enums.ChannelEventType
import persistence.jooq.funAlias
import persistence.jooq.tables.references.CHANNEL_EVENT
import persistence.jooq.tables.references.CHANNEL_MEMBER
import persistence.jooq.value
import persistence.jooq.tables.Channel as ChannelTable

fun detailedChannelRead2ToJson(channel: ChannelTable, lastEventType: Set<ChannelEventType>): JSONObjectNullStep<JSON> {
    val funName = ::detailedChannelRead2ToJson.name
    val innerChannelEvent = CHANNEL_EVENT.funAlias(funName)

    return jsonObject(
        DetailedChannelReadPayload2::id.value(channel.ID),
        DetailedChannelReadPayload2::name.value(channel.NAME),
        DetailedChannelReadPayload2::isManaged.value(channel.IS_MANAGED),
        DetailedChannelReadPayload2::members.value(
            select(detailedChannelMemberReadToJson(channelMember = CHANNEL_MEMBER))
                .from(CHANNEL_MEMBER)
                .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channel.ID))
        ),
        DetailedChannelReadPayload2::lastEvent.value(
            select(channelEventReadPayloadToJson(channelEvent = innerChannelEvent))
                .from(innerChannelEvent)
                .where(innerChannelEvent.CHANNEL_ID.eq(channel.ID))
                .and(innerChannelEvent.TYPE.`in`(lastEventType))
                .orderBy(innerChannelEvent.ID.desc())
                .limit(1),
        ),
        DetailedChannelReadPayload2::createdAt.value(channel.CREATED_AT),
        DetailedChannelReadPayload2::updatedAt.value(channel.UPDATED_AT),
    )
}
