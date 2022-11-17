package persistence.postgres.queries.channelmember

import org.jooq.impl.DSL.selectFrom
import org.jooq.impl.DSL.value
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.MESSAGE
import java.util.*

fun KotlinTransactionContext.isMemberOfChannelOf(userId: String, messageIds: Set<UUID>): Boolean {
    return db.fetchExists(
        selectFrom(MESSAGE).where(MESSAGE.ID.`in`(messageIds))
            .andNot(isMemberOfChannel(MESSAGE.CHANNEL_ID, userId = value(userId)))
    ).not()
}
