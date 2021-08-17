package testutil

import clientapi.AuthContext
import clientapi.models.MessageWritePayload
import persistence.jooq.enums.ChannelMemberRole
import models.ChannelMemberWritePayload
import models.ChannelWritePayload
import models.UserWritePayload
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
