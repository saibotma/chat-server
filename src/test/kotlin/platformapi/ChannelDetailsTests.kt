package platformapi

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.*
import testutil.PostgresTest
import testutil.mockedChannelWrite
import testutil.serverTest

class ChannelDetailsTests : PostgresTest() {
    @Nested
    inner class UpsertChannelTests {
        @Test
        fun `updates a channel when it already exists`() {
            serverTest {
                val channel = upsertChannel(mockedChannelWrite())
                val updatedChannel = upsertChannel(channel.copy(name = "updated name", isManaged = !channel.isManaged))

                with(getChannels().map { it.toChannelWrite() }.toList()) {
                    shouldHaveSize(1)
                    first() shouldBe updatedChannel
                }
            }
        }
    }
}
