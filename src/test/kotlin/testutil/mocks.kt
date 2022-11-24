package testutil

import clientapi.AuthContext
import clientapi.UserId
import clientapi.models.CreateChannelInputMember
import clientapi.models.MessageWritePayload
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import models.ChannelMemberWritePayload
import models.ChannelWritePayload
import models.UserWritePayload
import persistence.jooq.enums.ChannelMemberRole
import persistence.jooq.tables.pojos.ChannelMember
import persistence.jooq.tables.pojos.FirebasePushToken
import push.FirebaseInitializer
import push.PushNotification
import push.PushNotificationSender
import java.util.*

fun mockedChannelWrite(
    name: String? = null,
    description: String? = null,
    isManaged: Boolean = false,
): ChannelWritePayload {
    return ChannelWritePayload(
        name = name,
        description = description,
        isManaged = isManaged,
    )
}

fun mockedChannelMemberWrite(
    userId: String,
    role: ChannelMemberRole = ChannelMemberRole.user
): ChannelMemberWritePayload {
    return ChannelMemberWritePayload(userId = userId, role = role)
}

fun mockedCreateChannelInputMember(
    userId: String,
    role: ChannelMemberRole = ChannelMemberRole.user
): CreateChannelInputMember {
    return CreateChannelInputMember(userId = userId, role = role)
}

fun mockedUser(name: String = "name"): UserWritePayload {
    return UserWritePayload(name = name)
}

fun mockedAuthContext(userId: String) = AuthContext(userId = UserId(userId))

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

fun mockedChannelMember(channelId: UUID, userId: String, role: ChannelMemberRole): ChannelMember {
    return ChannelMember(channelId = channelId, userId = userId, role = ChannelMemberRole.admin)
}

fun mockedAdminChannelMember(channelId: UUID, userId: String) =
    mockedChannelMember(channelId = channelId, userId = userId, role = ChannelMemberRole.admin)

fun mockedFirebaseInitializer(): FirebaseInitializer {
    return mockk {
        every { init() } answers { }
    }
}
