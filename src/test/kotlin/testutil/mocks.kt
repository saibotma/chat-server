package testutil

import platformapi.models.ChannelWrite
import java.util.*
import java.util.UUID.randomUUID

fun mockedChannelWrite(): ChannelWrite {
    val id = randomUUID()
    return ChannelWrite(
        id = id,
        name = "name-$id",
        isManaged = false,
    )
}
