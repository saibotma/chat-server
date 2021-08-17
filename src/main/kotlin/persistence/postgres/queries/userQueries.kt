package persistence.postgres.queries

import persistence.jooq.tables.User.Companion.USER
import persistence.jooq.tables.pojos.User
import persistence.jooq.tables.records.UserRecord
import persistence.jooq.KotlinTransactionContext

fun KotlinTransactionContext.upsertUser(user: User) {
    db.insertInto(USER).set(UserRecord().apply { from(user) })
        .onDuplicateKeyUpdate()
        .set(USER.NAME, user.name)
        .execute()
}

fun KotlinTransactionContext.deleteUser(userId: String) {
    db.deleteFrom(USER).where(USER.ID.eq(userId)).execute()
}

fun KotlinTransactionContext.getUser(userId: String): User? {
    return db.select()
        .from(USER)
        .where(USER.ID.eq(userId))
        .fetchOneInto(User::class.java)
}
