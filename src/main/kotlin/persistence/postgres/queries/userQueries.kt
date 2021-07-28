package persistence.postgres.queries

import app.appella.persistence.jooq.KotlinTransactionContext
import dev.saibotma.persistence.postgres.jooq.tables.User.Companion.USER
import dev.saibotma.persistence.postgres.jooq.tables.pojos.User

fun KotlinTransactionContext.getUser(userId: String): User? {
    return db.select()
        .from(USER)
        .where(USER.ID.eq(userId))
        .fetchOneInto(User::class.java)
}
