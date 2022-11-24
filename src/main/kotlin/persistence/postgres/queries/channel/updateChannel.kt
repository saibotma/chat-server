package persistence.postgres.queries.channel

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.Channel
import util.Optional
import java.time.Instant
import java.util.*

fun KotlinTransactionContext.updateChannel(
    id: UUID,
    name: Optional<String?>?,
    description: Optional<String?>?,
    isManaged: Boolean? = null
) {
    db.update(Channel.CHANNEL)
        .apply {
            if (name != null) set(Channel.CHANNEL.NAME, name.value)
            if (description != null) set(Channel.CHANNEL.DESCRIPTION, description.value)
            if (isManaged != null) set(Channel.CHANNEL.IS_MANAGED, isManaged)
        }
        .set(Channel.CHANNEL.UPDATED_AT, Instant.now())
        .where(Channel.CHANNEL.ID.eq(id))
        .execute()
}
