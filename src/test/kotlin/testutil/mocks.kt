package testutil

import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import platformapi.models.ChannelMemberWritePayload
import platformapi.models.ChannelWritePayload
import platformapi.models.UserWritePayload
import java.util.*
import java.util.UUID.randomUUID

fun mockedChannelWrite(): ChannelWritePayload {
    val id = randomUUID()
    return ChannelWritePayload(
        name = "name-$id",
        isManaged = false,
    )
}

fun mockedChannelMemberWrite(userId: String): ChannelMemberWritePayload {
    return ChannelMemberWritePayload(userId = userId, role = ChannelMemberRole.user)
}

fun mockedUser(): UserWritePayload {
    val id = randomUUID().toString()
    return UserWritePayload(id = id, name = "name-$id")
}
