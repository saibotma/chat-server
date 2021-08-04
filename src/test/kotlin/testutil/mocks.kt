package testutil

import platformapi.models.ChannelWritePayload
import java.util.*
import java.util.UUID.randomUUID

fun mockedChannelWrite(): ChannelWritePayload {
    val id = randomUUID()
    return ChannelWritePayload(
        name = "name-$id",
        isManaged = false,
    )
}
