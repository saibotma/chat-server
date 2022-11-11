package platformapi

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import models.ChannelReadPayload
import models.ChannelWritePayload
import models.toChannelRead
import testutil.mockedChannelWrite
import testutil.servertest.post.createChannel
import testutil.servertest.serverTest

class ChannelListTests {
    @Nested
    inner class CreateChannelTests {
        @Test
        fun `creates a channel and returns it`() {
            serverTest {
                val (write, read) = createChannel(mockedChannelWrite())
                read?.toWrite() shouldBe write

                with(getChannels().map { it.toChannelRead() }) {
                    shouldHaveSize(1)
                    first() shouldBe read
                }
            }
        }
    }
}

fun ChannelReadPayload.toWrite(): ChannelWritePayload {
    return ChannelWritePayload(name = name, isManaged = isManaged)
}
