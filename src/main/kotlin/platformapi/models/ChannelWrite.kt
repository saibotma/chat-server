package platformapi.models

import dev.saibotma.persistence.postgres.jooq.tables.pojos.Channel
import dev.saibotma.persistence.postgres.jooq.tables.records.ChannelRecord
import java.time.Instant
import java.util.*

data class ChannelWrite(
    val id: UUID,
    val name: String?,
    val isManaged: Boolean,
)

fun ChannelWrite.toChannel(): Channel {
    return Channel(id = id, name = name, isManaged = isManaged, createdAt = Instant.now())
}

fun ChannelWrite.toChannelRecord(): ChannelRecord {
    return ChannelRecord(id = id, name = name, isManaged = isManaged)
}

fun Channel.toChannelWrite(): ChannelWrite {
    return ChannelWrite(id = id!!, name = name, isManaged = isManaged!!)
}
