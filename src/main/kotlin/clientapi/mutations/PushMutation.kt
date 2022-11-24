package clientapi.mutations

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.resourceNotFound
import persistence.jooq.KotlinDslContext
import persistence.jooq.tables.pojos.FirebasePushToken
import persistence.postgres.queries.deleteTokenByDeviceId
import persistence.postgres.queries.deviceIdBelongsToUser
import persistence.postgres.queries.upsertPushToken

class PushMutation(private val database: KotlinDslContext) {
    suspend fun upsertPushToken(
        context: AuthContext,
        deviceId: String,
        pushToken: String
    ): Boolean {
        database.transaction {
            upsertPushToken(FirebasePushToken(userId = context.userId.value, deviceId = deviceId, pushToken = pushToken))
        }

        return true
    }

    suspend fun deletePushToken(
        context: AuthContext,
        deviceId: String,
    ): Boolean {
        database.transaction {
            if (!deviceIdBelongsToUser(deviceId = deviceId, userId = context.userId)) {
                throw ClientApiException.resourceNotFound()
            }
            deleteTokenByDeviceId(deviceId = deviceId)
        }

        return true
    }
}
