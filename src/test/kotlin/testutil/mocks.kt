package testutil

import models.DetailedChannel
import java.util.*
import java.util.UUID.randomUUID

fun mockedChannelWrite(): DetailedChannel {
    val id = randomUUID()
    return DetailedChannel(
        id = id,
        name = "name-$id",
        isManaged = false,
    )
}
