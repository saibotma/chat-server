package platformapi.models

import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember

// TODO(saibotma): Maybe rename
data class ChannelWrite(
    val meta: ChannelMeta,
    val members: List<ChannelMember>,
)
