package testutil

import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import platformapi.models.ChannelMemberWritePayload
import platformapi.models.ChannelWritePayload
import platformapi.models.UserWritePayload
import java.util.*
import java.util.UUID.randomUUID

fun mockedChannelWrite(isManaged: Boolean = false): ChannelWritePayload {
    val id = randomUUID()
    return ChannelWritePayload(
        name = "name-$id",
        isManaged = isManaged,
    )
}

fun mockedChannelMemberWrite(
    userId: String,
    role: ChannelMemberRole = ChannelMemberRole.user
): ChannelMemberWritePayload {
    return ChannelMemberWritePayload(userId = userId, role = role)
}

fun mockedUser(): UserWritePayload {
    val id = randomUUID().toString()
    return UserWritePayload(id = id, name = "name-$id")
}
