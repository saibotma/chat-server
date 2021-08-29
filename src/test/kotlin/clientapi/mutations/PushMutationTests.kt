package clientapi.mutations

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import persistence.jooq.tables.pojos.FirebasePushToken
import testutil.mockedAuthContext
import testutil.serverTest

class PushMutationTests {
    @Nested
    inner class UpsertPushTokenTests {
        @Test
        fun `upserts a push token`() {
            serverTest {
                val (_, user) = upsertUser()
                val context = mockedAuthContext(user!!.id)
                pushMutation.upsertPushToken(context, deviceId = "DeviceId1", pushToken = "PushToken1")

                getFirebasePushTokens() shouldContainExactlyInAnyOrder listOf(
                    FirebasePushToken(
                        userId = context.userId,
                        deviceId = "DeviceId1",
                        pushToken = "PushToken1"
                    )
                )
            }
        }

        @Test
        fun `upsert a push token should do nothing when the same token is given multiple times`() =
            serverTest {
                val (_, user) = upsertUser()
                val context = mockedAuthContext(user!!.id)
                pushMutation.upsertPushToken(context, deviceId = "DeviceId1", pushToken = "PushToken1")
                pushMutation.upsertPushToken(context, deviceId = "DeviceId1", pushToken = "PushToken1")

                getFirebasePushTokens() shouldContainExactlyInAnyOrder listOf(
                    FirebasePushToken(
                        userId = context.userId,
                        deviceId = "DeviceId1",
                        pushToken = "PushToken1"
                    )
                )
            }

        @Test
        fun `adding a different push token for the same device, regardless of the user, should update the entity`() {
            serverTest {
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()
                val context1 = mockedAuthContext(user1!!.id)
                val context2 = mockedAuthContext(user2!!.id)
                pushMutation.upsertPushToken(context1, deviceId = "DeviceId1", pushToken = "PushToken1")
                pushMutation.upsertPushToken(context2, deviceId = "DeviceId1", pushToken = "DifferentToken")

                getFirebasePushTokens() shouldContainExactlyInAnyOrder listOf(
                    FirebasePushToken(
                        userId = context2.userId,
                        deviceId = "DeviceId1",
                        pushToken = "DifferentToken"
                    )
                )
            }
        }
    }

    @Nested
    inner class DeletePushTokenTests {
        @Test
        fun `removes the push token of a device`() {
            serverTest {
                val (_, user) = upsertUser()
                val context = mockedAuthContext(user!!.id)
                pushMutation.upsertPushToken(context, deviceId = "DeviceId1", pushToken = "PushToken1")
                pushMutation.upsertPushToken(context, deviceId = "OtherDevice", pushToken = "DifferentToken")
                pushMutation.deletePushToken(context, deviceId = "DeviceId1")

                getFirebasePushTokens() shouldContainExactlyInAnyOrder listOf(
                    FirebasePushToken(
                        userId = context.userId,
                        deviceId = "OtherDevice",
                        pushToken = "DifferentToken"
                    )
                )
            }
        }
    }
}
