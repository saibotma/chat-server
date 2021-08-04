package platformapi

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testutil.mockedChannelWrite
import testutil.serverTest

class ChannelListTests {
    @Nested
    inner class CreateChannelTests {
        @Test
        fun `creates a channel`() {
            serverTest {
                val channel = upsertChannel(mockedChannelWrite())

                with(getChannels().map { it.toChannelWrite() }.toList()) {
                    shouldHaveSize(1)
                    first() shouldBe channel
                }
            }
        }
    }
}
