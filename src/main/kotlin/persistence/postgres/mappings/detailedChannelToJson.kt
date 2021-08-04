package persistence.postgres.mappings

import clientapi.models.DetailedChannel
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL_MEMBER
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import org.jooq.impl.DSL.select
import persistence.jooq.jsonArrayAggNoNull
import persistence.jooq.value
import dev.saibotma.persistence.postgres.jooq.tables.Channel as ChannelTable

fun detailedChannelToJson(channel: ChannelTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        DetailedChannel::id.value(channel.ID),
        DetailedChannel::name.value(channel.NAME),
        DetailedChannel::isManaged.value(channel.IS_MANAGED),
        DetailedChannel::members.value(
            select(jsonArrayAggNoNull(detailedChannelMemberToJson(CHANNEL_MEMBER)))
                .from(CHANNEL_MEMBER)
                .where(CHANNEL_MEMBER.CHANNEL_ID.eq(channel.ID))
        )
    )
}
