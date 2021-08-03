package persistence.postgres.mappings

import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import persistence.jooq.value
import models.ChannelMeta
import dev.saibotma.persistence.postgres.jooq.tables.Channel as ChannelTable

fun channelMetaToJson(channel: ChannelTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        ChannelMeta::id.value(channel.ID),
        ChannelMeta::name.value(channel.NAME),
        ChannelMeta::isManaged.value(channel.IS_MANAGED)
    )
}
