package platformapi

import persistence.jooq.enums.ChannelMemberRole
import error.ApiException
import error.managedChannelHasAdmin
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import models.ChannelMemberReadPayload
import models.ChannelMemberWritePayload
import models.toChannelMemberRead
import testutil.mockedChannelMember
import testutil.mockedChannelWrite
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
                    member = mockedChannelMember(userId = user!!.id)
                )

                read!!.toWrite() shouldBe write

                with(getMembers().map { it.toChannelMemberRead() }) {
                    shouldHaveSize(1)
                    first() shouldBe read
                }
            }
        }

        @Test
        fun `returns an error when a managed channel has an admin`() {
            serverTest {
                val (_, channel) = createChannel(mockedChannelWrite(isManaged = true))
                val (_, user) = createUser()
                addMember(
                    channelId = channel!!.id,
                    member = mockedChannelMember(userId = user!!.id, role = ChannelMemberRole.admin)
                ) { _, _ ->
                    status() shouldBe HttpStatusCode.BadRequest
                    asApiError() shouldBe ApiException.managedChannelHasAdmin().error
                }
            }
        }
    }

    @Nested
    inner class UpdateMembersTests {
        @Test
        fun `replaces all members of a channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user1) = createUser()
                val (_, user2) = createUser()
                val (_, user3) = createUser()
                addMember(channelId = channel!!.id, member = mockedChannelMember(userId = user1!!.id))
                addMember(
                    channelId = channel.id,
                    member = mockedChannelMember(userId = user2!!.id, role = ChannelMemberRole.user)
                )
                // Delete 1, edit 2 and add 3
                val (write, read) = setMembers(
                    channelId = channel.id,
                    members = listOf(
                        mockedChannelMember(userId = user2.id, role = ChannelMemberRole.admin),
                        mockedChannelMember(userId = user3!!.id),
                    )
                )

                read!!.map { it.toWrite() } shouldContainExactlyInAnyOrder write
                getMembers().map { it.toChannelMemberRead() } shouldContainExactlyInAnyOrder read
            }
        }

        @Test
        fun `returns an error when a managed channel has an admin`() {
            serverTest {
                val (_, channel) = createChannel(mockedChannelWrite(isManaged = true))
                val (_, user) = createUser()
                setMembers(
                    channelId = channel!!.id,
                    members = listOf(mockedChannelMember(userId = user!!.id, role = ChannelMemberRole.admin))
                ) { _, _ ->
                    status() shouldBe HttpStatusCode.BadRequest
                    asApiError() shouldBe ApiException.managedChannelHasAdmin().error
                }
            }
        }
    }
}

private fun ChannelMemberReadPayload.toWrite(): ChannelMemberWritePayload {
    return ChannelMemberWritePayload(userId = userId, role = role)
}
