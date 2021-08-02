package persistence.postgres.queries

import persistence.jooq.KotlinTransactionContext
import dev.saibotma.persistence.postgres.jooq.tables.references.CHANNEL
import persistence.postgres.catchPostgresExceptions
import persistence.postgres.isUniqueViolation
import platformapi.models.ChannelWrite
import platformapi.models.toChannelRecord
import util.Fallible

sealed class InsertChannelError {
    object Duplicate : InsertChannelError()
}

fun KotlinTransactionContext.insertChannel(channel: ChannelWrite): Fallible<InsertChannelError, Unit> {
    return catchPostgresExceptions {
        db.insertInto(CHANNEL)
            .set(CHANNEL.ID, channel.id)
            .set(CHANNEL.NAME, channel.name)
            .set(CHANNEL.IS_MANAGED, channel.isManaged)
            .execute()
        Unit
    }.onFailure {
        when {
            isUniqueViolation(CHANNEL.primaryKey.name) -> InsertChannelError.Duplicate
            else -> throw this
        }
    }

}

fun KotlinTransactionContext.updateChannel(channel: ChannelWrite) {
    db.update(CHANNEL)
        .set(CHANNEL.ID, channel.id)
        .set(CHANNEL.NAME, channel.name)
        .set(CHANNEL.IS_MANAGED, channel.isManaged)
        .execute()
}
