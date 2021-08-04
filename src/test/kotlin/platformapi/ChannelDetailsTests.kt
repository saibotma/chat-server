package platformapi

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import platformapi.models.toChannelRead
import testutil.PostgresTest
import testutil.mockedChannelWrite
import testutil.serverTest

class ChannelDetailsTests : PostgresTest() {
    @Nested
    inner class UpsertChannelTests {
        @Test
        fun `updates an existing channel and returns it`() {
            serverTest {
                val (channelWrite, channelRead) = createChannel(mockedChannelWrite())
                val (updatedChannelWrite, updatedChannelRead) =
                    updateChannel(
                        id = channelRead!!.id,
                        channelWrite.copy(name = "updated name", isManaged = !channelWrite.isManaged)
                    )
                updatedChannelRead?.toWrite() shouldBe updatedChannelWrite

                with(getChannels().map { it.toChannelRead() }) {
                    shouldHaveSize(1)
                    first() shouldBe updatedChannelRead
                }
            }
        }
    }
}
