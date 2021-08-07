package platformapi

import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import models.toChannelMemberWrite
import testutil.mockedChannelMember
import testutil.serverTest

class ChannelMemberDetailsTests {
    @Nested
    inner class UpdateMemberTests {
        @Test
        fun `updates a member and returns it`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user1) = createUser()
                val (_, user2) = createUser()
                val (memberWrite1, memberRead1) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = user1!!.id, role = ChannelMemberRole.admin)
                )
                val (memberWrite2, _) = addMember(
                    channelId = channel.id,
                    mockedChannelMember(userId = user2!!.id, role = ChannelMemberRole.admin)
                )
                val (updatedMemberWrite, updatedMemberRead) = updateMember(
                    channelId = channel.id,
                    memberWrite1.copy(role = ChannelMemberRole.user)
                )

                updatedMemberRead shouldBe memberRead1!!.copy(role = ChannelMemberRole.user)
                getMembers().map { it.toChannelMemberWrite() }
                    .shouldContainExactlyInAnyOrder(listOf(updatedMemberWrite, memberWrite2))
            }
        }
    }

    @Nested
    inner class DeleteMemberTests {
        // TODO(saibotma): Also test with same user in different channel as for client api test.
        @Test
        fun `deletes a member`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user1) = createUser()
                val (_, user2) = createUser()
                val (memberWrite1, _) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = user1!!.id)
                )
                val (memberWrite2, _) = addMember(
                    channelId = channel.id,
                    mockedChannelMember(userId = user2!!.id)
                )
                deleteMember(channelId = channel.id, userId = memberWrite1.userId)
                with(getMembers().map { it.toChannelMemberWrite() }) {
                    shouldHaveSize(1)
                    first().userId shouldBe memberWrite2.userId
                }
            }
        }
    }
}
