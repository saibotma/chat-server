package persistence.postgres.queries.userevent

import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.pojos.UserEvent
import persistence.jooq.tables.references.USER_EVENT

fun KotlinTransactionContext.getUserEvent(id: Long): UserEvent? {
    return db.selectFrom(USER_EVENT)
        .where(USER_EVENT.ID.eq(id))
        .fetchOneInto(UserEvent::class.java)
}
