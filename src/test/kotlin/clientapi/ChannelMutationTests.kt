package clientapi

import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import models.toChannelRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import platformapi.models.toChannelMemberRead
import platformapi.models.toChannelMemberWrite
import testutil.mockedAuthContext
import testutil.mockedChannelMember
import testutil.mockedChannelWrite
import testutil.serverTest

class ChannelMutationTests {
    @Nested
    inner class AddChannelTests {
        @Test
        fun `creates a channel and returns it`() {
            serverTest {
                val (_, user1) = createUser()
                val (_, user2) = createUser()

                val context = mockedAuthContext(userId = user1!!.id)
                val members = listOf(mockedChannelMember(user1.id), mockedChannelMember(user2!!.id))
                val detailedChannel = channelMutation.createChannel(
                    context = context,
                    name = "Channel",
                    members = members,
                )

                with(getChannels()) {
                    shouldHaveSize(1)
                    first().name shouldBe "Channel"
                    detailedChannel.id shouldBe first().id
                }

                with(getMembers().map { it.toChannelMemberWrite() }) {
                    shouldHaveSize(2)
                    shouldContainExactlyInAnyOrder(members)
                }
            }
        }
    }

    @Nested
    inner class UpdateChannelTests {
        @Test
        fun `updates a channel when the user is an admin of the channel and returns it`() {
            serverTest {
                val (_, channel) = createChannel(mockedChannelWrite(name = "Channel"))
                // Add another channel to check that only the specified one gets updated
                val (_, otherChannel) = createChannel(mockedChannelWrite(name = "Channel"))
                val (_, admin) = createUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin.id)
                val detailedChannel =
                    channelMutation.updateChannel(context = context, id = channel.id, name = "Updated channel")

                detailedChannel.id shouldBe channel.id
                getChannels().map { it.toChannelRead() }
                    .shouldContainExactlyInAnyOrder(listOf(channel.copy(name = "Updated channel"), otherChannel))
            }
        }

        @Test
        fun `returns an error when the user is not an admin of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user) = createUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = user!!.id, role = ChannelMemberRole.user)
                )

                val context = mockedAuthContext(userId = user.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.updateChannel(context = context, id = channel.id, name = "Updated channel")
                }

                error shouldBe ClientApiException.resourceNotFound()
                getChannels().first().name shouldBe channel.name
            }
        }
    }

    @Nested
    inner class DeleteChannelTests {
        @Test
        fun `deletes a channel when the user is an admin of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, otherChannel) = createChannel()
                val (_, admin) = createUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin.id)
                channelMutation.deleteChannel(context = context, id = channel.id)

                with(getChannels()) {
                    shouldHaveSize(1)
                    first().id shouldBe otherChannel!!.id
                }
            }
        }

        @Test
        fun `returns an error when the user is not an admin of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user) = createUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = user!!.id, role = ChannelMemberRole.user)
                )

                val context = mockedAuthContext(userId = user.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.deleteChannel(context = context, id = channel.id)
                }

                error shouldBe ClientApiException.resourceNotFound()
                getChannels() shouldHaveSize 1
            }
        }
    }

    @Nested
    inner class UpsertMemberTest {
        @Test
        fun `adds a member and returns it when the user is an admin`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin) = createUser()
                val (_, user) = createUser()
                val (adminMember, _) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin.id)
                val otherMember = mockedChannelMember(userId = user!!.id, role = ChannelMemberRole.admin)
                val detailedChannelMember = channelMutation.upsertMember(
                    context = context,
                    channelId = channel.id,
                    member = otherMember,
                )

                detailedChannelMember.userId shouldBe user.id
                getMembers().map { it.toChannelMemberWrite() }
                    .shouldContainExactlyInAnyOrder(listOf(adminMember, otherMember))
            }
        }

        @Test
        fun `updates a member and returns it when the user is an admin and the member already exists`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin) = createUser()
                val (member, _) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin.id)
                val updatedMember = member.copy(role = ChannelMemberRole.user)
                val detailedChannelMember = channelMutation.upsertMember(
                    context = context,
                    channelId = channel.id,
                    member = updatedMember,
                )

                detailedChannelMember.userId shouldBe admin.id
                with(getMembers().map { it.toChannelMemberWrite() }) {
                    shouldHaveSize(1)
                    first() shouldBe updatedMember
                }
            }
        }

        @Test
        fun `returns an error when the user is not an admin of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user) = createUser()
                val (member, _) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = user!!.id, role = ChannelMemberRole.user)
                )

                val context = mockedAuthContext(userId = user.id)
                val updatedMember = member.copy(role = ChannelMemberRole.admin)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.upsertMember(
                        context = context,
                        channelId = channel.id,
                        member = updatedMember,
                    )
                }

                error shouldBe ClientApiException.resourceNotFound()
                getMembers().first().toChannelMemberWrite() shouldBe member
            }
        }
    }

    @Nested
    inner class RemoveMemberTests {
        @Test
        fun `removes a member when the user is an admin of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, otherChannel) = createChannel()
                val (_, admin) = createUser()
                val (_, otherAdmin) = createUser()
                val (_, member1) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = admin!!.id, role = ChannelMemberRole.admin)
                )
                addMember(
                    channelId = channel.id,
                    mockedChannelMember(userId = otherAdmin!!.id, role = ChannelMemberRole.admin)
                )
                // Have the same member as a member of another channel to see that only the specified one gets removed.
                val (_, member2) = addMember(
                    channelId = otherChannel!!.id,
                    mockedChannelMember(userId = otherAdmin.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin.id)
                channelMutation.removeMember(
                    context = context,
                    channelId = channel.id,
                    userId = otherAdmin.id
                )

                getMembers().map { it.toChannelMemberRead() }.shouldContainExactlyInAnyOrder(listOf(member1, member2))
            }
        }

        @Test
        fun `removes a member when the user is the same as the removed member`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin) = createUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin.id)
                channelMutation.removeMember(
                    context = context,
                    channelId = channel.id,
                    userId = admin.id
                )

                getMembers() shouldHaveSize 0
            }
        }

        @Test
        fun `returns an error when the user is not an admin or the removed member`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user1) = createUser()
                val (_, user2) = createUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMember(userId = user1!!.id, role = ChannelMemberRole.user)
                )
                addMember(
                    channelId = channel.id,
                    mockedChannelMember(userId = user2!!.id, role = ChannelMemberRole.user)
                )

                val context = mockedAuthContext(userId = user1.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.removeMember(
                        context = context,
                        channelId = channel.id,
                        userId = user2.id
                    )
                }

                error shouldBe ClientApiException.resourceNotFound()
                getMembers() shouldHaveSize 2
            }
        }

        @Test
        fun `returns an error when the channel is managed`() {
            serverTest {
                val (_, channel) = createChannel(mockedChannelWrite(isManaged = true))
                val (_, user) = createUser()
                addMember(channelId = channel!!.id, mockedChannelMember(userId = user!!.id))

                val context = mockedAuthContext(userId = user.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.removeMember(
                        context = context,
                        channelId = channel.id,
                        userId = user.id
                    )
                }

                error shouldBe ClientApiException.resourceNotFound()
                getMembers() shouldHaveSize 1
            }
        }
    }
}
