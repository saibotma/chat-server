package platformapi.models

import dev.saibotma.persistence.postgres.jooq.tables.pojos.Channel
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelRecord
import java.time.Instant
import java.util.*

interface ChannelPayload {
    val name: String?
    val isManaged: Boolean
}

data class ChannelWritePayload(override val name: String?, override val isManaged: Boolean) : ChannelPayload

fun ChannelWritePayload.toChannel(id: UUID, createdAt: Instant): Channel {
    return Channel(id = id, name = name, isManaged = isManaged, createdAt = createdAt)
}
