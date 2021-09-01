package testutil

import clientapi.AuthContext
import clientapi.models.MessageWritePayload
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import persistence.jooq.enums.ChannelMemberRole
import models.ChannelMemberWritePayload
import models.ChannelWritePayload
import models.UserWritePayload
import persistence.jooq.tables.pojos.FirebasePushToken
import push.FirebaseInitializer
import push.PushNotification
import push.PushNotificationSender
import java.util.*

fun mockedChannelWrite(name: String? = null, isManaged: Boolean = false): ChannelWritePayload {
    return ChannelWritePayload(
        name = name,
        isManaged = isManaged,
    )
}

fun mockedChannelMember(
    userId: String,
    role: ChannelMemberRole = ChannelMemberRole.user
): ChannelMemberWritePayload {
    return ChannelMemberWritePayload(userId = userId, role = role)
}

fun mockedUser(name: String = "name"): UserWritePayload {
    return UserWritePayload(name = name)
}

fun mockedAuthContext(userId: String) = AuthContext(userId = userId)

fun mockedMessage(text: String? = null, respondedMessageId: UUID? = null): MessageWritePayload {
    return MessageWritePayload(text = text, repliedMessageId = respondedMessageId)
}

fun mockedPushNotificationSender(
    onSend: (pushToken: FirebasePushToken, notification: PushNotification) -> Unit = { _, _ -> }
): PushNotificationSender {
    return mockk {
        coEvery { send(any(), any()) } answers {
            onSend(args[0] as FirebasePushToken, args[1] as PushNotification)
            true
        }
    }
}

fun mockedFirebaseInitializer(): FirebaseInitializer {
    return mockk {
        every { init() } answers { }
    }
}
