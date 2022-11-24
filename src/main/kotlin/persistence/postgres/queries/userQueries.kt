package persistence.postgres.queries

import clientapi.UserId
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.User.Companion.USER
import persistence.jooq.tables.pojos.User
import persistence.jooq.tables.records.UserRecord

fun KotlinTransactionContext.upsertUser(user: User) {
    db.insertInto(USER).set(UserRecord().apply { from(user) })
        .onDuplicateKeyUpdate()
        .set(USER.NAME, user.name)
        .execute()
}

fun KotlinTransactionContext.deleteUser(userId: UserId) {
    db.deleteFrom(USER).where(USER.ID.eq(userId.value)).execute()
}

fun KotlinTransactionContext.getUser(userId: UserId): User? {
    return db.select()
        .from(USER)
        .where(USER.ID.eq(userId.value))
        .fetchOneInto(User::class.java)
}
