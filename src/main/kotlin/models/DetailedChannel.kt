package models

import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember

data class DetailedChannel(
    val meta: ChannelMeta,
    val members: List<ChannelMember>,
)
