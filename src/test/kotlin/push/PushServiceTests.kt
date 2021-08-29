package push

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import models.UserReadPayload
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import persistence.jooq.tables.pojos.FirebasePushToken
import testutil.*
import java.util.*

class PushServiceTests {
    @Nested
    inner class NewMessageTests {
        @Test
        fun `sends a notification to all channel members except the message creator`() {
            val tokens: MutableList<FirebasePushToken> = mutableListOf()
            val notifications: MutableList<PushNotification> = mutableListOf()
            withPreparedDependencies(
                onNotification = { token, notification ->
                    tokens.add(token)
                    notifications.add(notification)
                },
                test = {
                    val (_, channel) = createChannel()
                    // Create another channel to check that only users from the specified channel get notified
                    val (_, otherChannel) = createChannel()

                    val user1 = addUserWithPushToken(deviceId = "device1", pushToken = "token1")
                    val user2 = addUserWithPushToken(deviceId = "device2", pushToken = "token2")
                    val user3 = addUserWithPushToken(deviceId = "device3", pushToken = "token3")
                    val user4 = addUserWithPushToken(deviceId = "device4", pushToken = "token4")

                    addMember(channelId = channel!!.id, member = mockedChannelMember(userId = user1.id))
                    addMember(channelId = channel.id, member = mockedChannelMember(userId = user2.id))
                    addMember(channelId = channel.id, member = mockedChannelMember(userId = user3.id))
                    addMember(channelId = otherChannel!!.id, member = mockedChannelMember(userId = user4.id))

                    val context = mockedAuthContext(userId = user1.id)
                    messageMutation.sendMessage(context, channelId = channel.id, message = mockedMessage("Hi"))



                    tokens shouldContainExactlyInAnyOrder listOf(
                        FirebasePushToken(userId = user2.id, deviceId = "device2", pushToken = "token2"),
                        FirebasePushToken(userId = user3.id, deviceId = "device3", pushToken = "token3"),
                    )

                    notifications.toSet() shouldContainExactlyInAnyOrder listOf(
                        PushNotification.Channel.NewMessage(
                            creatorName = user1.name!!,
                            channelName = null,
                            text = "Hi",
                            channelId = channel.id
                        )
                    )
                },
            )
        }

        @Test
        fun `does include the channel name when it is set`() {
            var notification: PushNotification? = null
            withPreparedDependencies(
                onNotification = { _, innerNotification -> notification = innerNotification },
                test = {
                    val (_, channel) = createChannel(mockedChannelWrite(name = "channel"))

                    val user1 = addUserWithPushToken(deviceId = "device1", pushToken = "token1")
                    val user2 = addUserWithPushToken(deviceId = "device2", pushToken = "token2")

                    addMember(channelId = channel!!.id, member = mockedChannelMember(userId = user1.id))
                    addMember(channelId = channel.id, member = mockedChannelMember(userId = user2.id))

                    val context = mockedAuthContext(userId = user1.id)
                    messageMutation.sendMessage(context, channelId = channel.id, message = mockedMessage("Hi"))

                    notification shouldBe PushNotification.Channel.NewMessage(
                        creatorName = user1.name!!,
                        channelName = "channel",
                        text = "Hi",
                        channelId = channel.id
                    )
                },
            )
        }
    }

    private suspend fun ServerTestEnvironment.addUserWithPushToken(
        deviceId: String,
        pushToken: String
    ): UserReadPayload {
        val userId = UUID.randomUUID().toString()
        val (_, user) = upsertUser(id = userId, mockedUser(name = userId))
        val context = mockedAuthContext(userId = user!!.id)
        pushMutation.upsertPushToken(context = context, deviceId = deviceId, pushToken = pushToken)
        return user
    }

    private fun withPreparedDependencies(
        onNotification: (token: FirebasePushToken, notification: PushNotification) -> Unit,
        test: suspend ServerTestEnvironment.() -> Unit
    ) {
        serverTest(
            bindDependencies = {
                bind<PushNotificationSender>(overrides = true) with singleton {
                    mockedPushNotificationSender(onNotification)
                }
                bind<PushService>(overrides = true) with singleton {
                    PushService(instance(), instance(), sendNotificationsBlocking = true)
                }
            },
            test = test,
        )
    }
}
