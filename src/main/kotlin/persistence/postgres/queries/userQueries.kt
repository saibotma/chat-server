package persistence.postgres.queries

import persistence.jooq.KotlinTransactionContext
import dev.saibotma.persistence.postgres.jooq.tables.User.Companion.USER
import dev.saibotma.persistence.postgres.jooq.tables.pojos.User
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.reactive.asFlow

suspend fun KotlinTransactionContext.getUser(userId: String): User? {
    return db.select()
        .from(USER)
        .where(USER.ID.eq(userId))
        .asFlow()
        .singleOrNull()
        ?.into(User::class.java)
}
