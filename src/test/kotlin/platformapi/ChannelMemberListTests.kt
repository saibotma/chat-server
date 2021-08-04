package platformapi

import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import platformapi.models.ChannelMemberReadPayload
import platformapi.models.ChannelMemberWritePayload
import testutil.mockedChannelMemberWrite
import testutil.serverTest

class ChannelMemberListTests {
    @Nested
    inner class CreateMemberTests {
        @Test
        fun `adds a user to a channel and returns it`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user) = createUser()
                val (write, read) = addMember(
                    channelId = channel!!.id,
                    member = mockedChannelMemberWrite(userId = user!!.id)
                )

                read!!.toWrite() shouldBe write

                with(getMembers().map { it.toChannelMemberRead() }) {
                    shouldHaveSize(1)
                    first() shouldBe read
                }
            }
        }
    }
}

private fun ChannelMemberReadPayload.toWrite(): ChannelMemberWritePayload {
    return ChannelMemberWritePayload(userId = userId, role = role)
}

private fun ChannelMember.toChannelMemberRead(): ChannelMemberReadPayload {
    return ChannelMemberReadPayload(channelId = channelId!!, userId = userId!!, role = role!!, addedAt = addedAt!!)
}
