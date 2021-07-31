package persistence.postgres.queries

import app.appella.persistence.jooq.KotlinTransactionContext
import dev.saibotma.persistence.postgres.jooq.tables.User.Companion.USER
import dev.saibotma.persistence.postgres.jooq.tables.pojos.User
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.reactive.asFlow

suspend fun KotlinTransactionContext.getUser(userId: String): User? {
    return db.select()
        .from(USER)
        .where(USER.ID.eq(userId))
        .asFlow()
        .firstOrNull()
        ?.into(User::class.java)
}
