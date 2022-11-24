package persistence.postgres.queries

import clientapi.UserId
import org.jooq.impl.DSL.exists
import org.jooq.impl.DSL.select
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.FirebasePushToken.Companion.FIREBASE_PUSH_TOKEN
import persistence.jooq.tables.pojos.FirebasePushToken
import persistence.jooq.tables.references.CHANNEL_MEMBER
import persistence.jooq.tables.references.USER
import java.util.*

fun KotlinTransactionContext.getPushTokenOfUsers(vararg userIds: String): List<FirebasePushToken> {
    return db.select(FIREBASE_PUSH_TOKEN.asterisk())
        .from(FIREBASE_PUSH_TOKEN)
        .where(FIREBASE_PUSH_TOKEN.USER_ID.`in`(userIds.toList()))
        .fetchInto(FirebasePushToken::class.java)
}

fun KotlinTransactionContext.deleteTokens(tokens: List<String>) {
    db.deleteFrom(FIREBASE_PUSH_TOKEN)
        .where(FIREBASE_PUSH_TOKEN.PUSH_TOKEN.`in`(tokens))
        .execute()
}

fun KotlinTransactionContext.deleteTokenByDeviceId(deviceId: String) {
    db.deleteFrom(FIREBASE_PUSH_TOKEN)
        .where(FIREBASE_PUSH_TOKEN.DEVICE_ID.eq(deviceId))
        .execute()
}

fun KotlinTransactionContext.getUserIdsOfChannel(channelId: UUID): List<String> {
    return db.select(USER.ID)
        .from(USER)
        .where(
            exists(
                select().from(CHANNEL_MEMBER).where(CHANNEL_MEMBER.USER_ID.eq(USER.ID))
                    .and(CHANNEL_MEMBER.CHANNEL_ID.eq(channelId))
            )
        ).fetchInto(String::class.java)
}

fun KotlinTransactionContext.upsertPushToken(firebasePushToken: FirebasePushToken) {
    db.insertInto(FIREBASE_PUSH_TOKEN)
        .set(FIREBASE_PUSH_TOKEN.USER_ID, firebasePushToken.userId)
        .set(FIREBASE_PUSH_TOKEN.DEVICE_ID, firebasePushToken.deviceId)
        .set(FIREBASE_PUSH_TOKEN.PUSH_TOKEN, firebasePushToken.pushToken)
        .onDuplicateKeyUpdate()
        .set(FIREBASE_PUSH_TOKEN.USER_ID, firebasePushToken.userId)
        .set(FIREBASE_PUSH_TOKEN.PUSH_TOKEN, firebasePushToken.pushToken)
        .execute()
}

fun KotlinTransactionContext.deviceIdBelongsToUser(deviceId: String, userId: UserId): Boolean {
    return db.fetchExists(
        select()
            .from(FIREBASE_PUSH_TOKEN)
            .where(FIREBASE_PUSH_TOKEN.DEVICE_ID.eq(deviceId))
            .and(FIREBASE_PUSH_TOKEN.USER_ID.eq(userId.value))
    )
}
