package persistence.postgres.mappings

import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL_MEMBER
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import org.jooq.impl.DSL.select
import persistence.jooq.value
import models.DetailedChannel
import dev.saibotma.persistence.postgres.jooq.tables.Channel as ChannelTable

fun detailedChannelToJson(channel: ChannelTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        DetailedChannel::meta.value(channelMetaToJson(channel)),
        DetailedChannel::members.value(
            jsonObject(
                *select().from(CHANNEL_MEMBER).where(CHANNEL_MEMBER.CHANNEL_ID.eq(channel.ID)).fields()
            )
        )
    )
}
