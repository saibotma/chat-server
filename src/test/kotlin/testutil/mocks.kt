package testutil

import clientapi.AuthContext
import clientapi.models.MessageWritePayload
import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import models.ChannelMemberWritePayload
import models.ChannelWritePayload
import models.UserWritePayload
import java.util.*
import java.util.UUID.randomUUID

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

fun mockedUser(): UserWritePayload {
    val id = randomUUID().toString()
    return UserWritePayload(id = id, name = "name-$id")
}

fun mockedAuthContext(userId: String) = AuthContext(userId = userId)

fun mockedMessage(text: String? = null, respondedMessageId: UUID? = null): MessageWritePayload {
    return MessageWritePayload(text = text, repliedMessageId = respondedMessageId)
}
