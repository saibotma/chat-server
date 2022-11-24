package clientapi.mutations

import clientapi.*
import clientapi.models.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import models.toChannelMemberRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import persistence.jooq.enums.ChannelMemberRole
import persistence.jooq.tables.pojos.Channel
import persistence.jooq.tables.pojos.ChannelMember
import testutil.*
import testutil.servertest.post.addMember
import testutil.servertest.post.createChannel
import testutil.servertest.put.upsertContact
import testutil.servertest.put.upsertUser
import testutil.servertest.serverTest
import util.fallbackTo

class ChannelMutationTests {
    @Nested
    inner class CreateChannelTests {
        @Test
        fun `creates a channel`() {
            serverTest {
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()

                val context = mockedAuthContext(userId = user1!!.id)
                // Don't add user1 as member, because that should happen automatically.
                val members = listOf(mockedCreateChannelInputMember(user2!!.id))

                upsertContact(userId1 = user1.id, userId2 = user2.id)

                channelMutation.createChannel(
                    context = context,
                    input = CreateChannelInput(
                        name = "Name",
                        description = "Description",
                        members = members,
                    )
                )

                val actualChannels = getChannels()
                val expectedChannels = listOf(
                    Channel(
                        name = "Name",
                        isManaged = false,
                        description = "Description",
                        creatorUserId = context.userId.value
                    )
                ).ignoreUnknown()
                actualChannels.ignoreUnknown() shouldContainExactlyInAnyOrder expectedChannels

                val channelId = actualChannels.first().id!!
                val actualMembers = getMembers().ignoreUnknown()
                val expectedMembers = (members.map { it.toChannelMember(channelId = channelId) } +
                        listOf(ChannelMember(channelId = channelId, userId = user1.id, role = ChannelMemberRole.admin)))
                    .ignoreUnknown()
                actualMembers shouldContainExactlyInAnyOrder expectedMembers
            }
        }

        @Test
        fun `returns an error when name is blank`() {
            serverTest {
                val (_, user1) = upsertUser()

                val context = mockedAuthContext(userId = user1!!.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.createChannel(
                        context = context,
                        input = CreateChannelInput(name = " ", description = null, members = emptyList())
                    )
                }

                error shouldBe ClientApiException.channelNameBlank()
                getChannels() shouldHaveSize 0
            }
        }

        @Test
        fun `returns an error when description is blank`() {
            serverTest {
                val (_, user1) = upsertUser()

                val context = mockedAuthContext(userId = user1!!.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.createChannel(
                        context = context,
                        input = CreateChannelInput(name = null, description = " ", members = emptyList())
                    )
                }

                error shouldBe ClientApiException.channelDescriptionBlank()
                getChannels() shouldHaveSize 0
            }
        }

        @Test
        fun `returns an error when any member is not a contact`() {
            serverTest {
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()

                val context = mockedAuthContext(userId = user1!!.id)
                val members = listOf(mockedCreateChannelInputMember(user2!!.id))
                val error = shouldThrow<ClientApiException> {
                    channelMutation.createChannel(
                        context = context,
                        input = CreateChannelInput(
                            name = "Name",
                            description = "Description",
                            members = members,
                        )
                    )
                }
                error shouldBe ClientApiException.resourceNotFound()

                getChannels() shouldHaveSize 0
                getMembers() shouldHaveSize 0
            }
        }
    }

    @Nested
    inner class UpdateChannelTests {
        fun test(updateInput: UpdateChannelInput) {
            serverTest {
                val (_, channel) = createChannel(mockedChannelWrite(name = "Name", description = "Description"))
                // Add another channel to check that only the specified one gets updated
                val (_, otherChannel) = createChannel(mockedChannelWrite(name = "Channel", description = "Description"))
                val (_, admin) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val previousChannel = getChannels().first { it.id == channel.id }
                val context = mockedAuthContext(userId = admin.id)
                channelMutation.updateChannel(context = context, id = channel.id, input = updateInput)

                // Also test that the other channel does not get updated.
                val actualChannels = getChannels().map(Channel::ignoreUnknown)
                val expectedChannels = listOf(
                    otherChannel!!.toChannel(), previousChannel.copy(
                        name = updateInput.name.fallbackTo(previousChannel.name),
                        description = updateInput.description.fallbackTo(previousChannel.description)
                    )
                ).map(Channel::ignoreUnknown)
                actualChannels shouldContainExactlyInAnyOrder expectedChannels
            }
        }

        @Test
        fun `updates the name when the user is an admin of the channel`() {
            test(updateInput = UpdateChannelInput(name = OptionalNullableString("Updated name")))
        }

        @Test
        fun `returns an error when the name is blank`() {
            serverTest {
                val (_, channel) = createChannel(mockedChannelWrite(name = "Name"))
                val (_, admin) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.updateChannel(
                        context = context,
                        id = channel.id,
                        input = UpdateChannelInput(name = OptionalNullableString("  "))
                    )
                }
                error shouldBe ClientApiException.channelNameBlank()

                val actualChannels = getChannels()
                actualChannels.first().name shouldBe channel.name
            }
        }

        @Test
        fun `updates the description when the user is an admin of the channel`() {
            test(updateInput = UpdateChannelInput(description = OptionalNullableString("Updated description")))
        }

        @Test
        fun `returns an error when the description is blank`() {
            serverTest {
                val (_, channel) = createChannel(mockedChannelWrite(description = "Description"))
                val (_, admin) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.updateChannel(
                        context = context,
                        id = channel.id,
                        input = UpdateChannelInput(description = OptionalNullableString("  "))
                    )
                }
                error shouldBe ClientApiException.channelDescriptionBlank()

                val actualChannels = getChannels()
                actualChannels.first().description shouldBe channel.description
            }
        }

        @Test
        fun `returns an error when the user is not an admin of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = user!!.id, role = ChannelMemberRole.user)
                )

                val context = mockedAuthContext(userId = user.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.updateChannel(context = context, id = channel.id, input = UpdateChannelInput())
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
                val (_, admin) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
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
                val (_, user) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = user!!.id, role = ChannelMemberRole.user)
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
    inner class AddMemberTest {
        @Test
        fun `adds a member when the user is an admin and contact`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin) = upsertUser()
                val (adminMember, _) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val (_, user) = upsertUser()
                upsertContact(userId1 = admin.id, userId2 = user!!.id)

                val context = mockedAuthContext(userId = admin.id)
                channelMutation.addMember(
                    context = context,
                    channelId = channel.id,
                    userId = user.id,
                    input = AddMemberInput(role = ChannelMemberRole.admin),
                )

                val actualMembers = getMembers().ignoreUnknown()
                val expectedMembers = listOf(
                    mockedChannelMember(channelId = channel.id, userId = user.id, role = ChannelMemberRole.admin),
                    adminMember.toChannelMember(channel.id)
                ).ignoreUnknown()
                actualMembers shouldContainExactlyInAnyOrder expectedMembers
            }
        }

        @Test
        fun `returns an error when the user is not an admin of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, notAdmin) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = notAdmin!!.id, role = ChannelMemberRole.user)
                )
                val (_, user) = upsertUser()
                upsertContact(userId1 = notAdmin.id, userId2 = user!!.id)

                val context = mockedAuthContext(userId = notAdmin.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.addMember(
                        context = context,
                        channelId = channel.id,
                        userId = user.id,
                        input = AddMemberInput(role = ChannelMemberRole.user),
                    )
                }

                error shouldBe ClientApiException.resourceNotFound()
                getMembers().first().userId shouldBe notAdmin.id
            }
        }

        @Test
        fun `returns an error when the user is not a contact of the new member`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
                )
                val (_, user) = upsertUser()

                val context = mockedAuthContext(userId = admin.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.addMember(
                        context = context,
                        channelId = channel.id,
                        userId = user!!.id,
                        input = AddMemberInput(role = ChannelMemberRole.user),
                    )
                }

                error shouldBe ClientApiException.resourceNotFound()
                getMembers().first().userId shouldBe admin.id
            }
        }
    }

    @Nested
    inner class UpdateMemberTests {
        fun test(updateInput: UpdateMemberInput) {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin) = upsertUser()
                val (adminMember, _) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
                )
                val (_, user) = upsertUser()
                addMember(
                    channelId = channel.id,
                    mockedChannelMemberWrite(userId = user!!.id, role = ChannelMemberRole.user)
                )

                val previousMember = getMembers().first { it.userId == user.id }
                val context = mockedAuthContext(userId = admin.id)
                channelMutation.updateMember(
                    context = context,
                    channelId = channel.id,
                    userId = user.id,
                    input = UpdateMemberInput(role = OptionalChannelMemberRole(ChannelMemberRole.admin)),
                )

                // Also test that the other members does not get updated.
                val actualMembers = getMembers().map(ChannelMember::ignoreUnknown)
                val expectedMembers = listOf(
                    adminMember.toChannelMember(channelId = channel.id),
                    previousMember.copy(role = updateInput.role.fallbackTo(previousMember.role!!))
                ).map(ChannelMember::ignoreUnknown)
                actualMembers.shouldContainExactlyInAnyOrder(expectedMembers)
            }
        }

        @Test
        fun `updates the role when the executing user is an admin`() {
            test(updateInput = UpdateMemberInput(role = OptionalChannelMemberRole(ChannelMemberRole.admin)))
        }

        @Test
        fun `returns an error when the user is not an admin of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, notAnAdmin) = upsertUser()
                val (notAnAdminMember, _) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = notAnAdmin!!.id, role = ChannelMemberRole.user)
                )

                val context = mockedAuthContext(userId = notAnAdmin.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.updateMember(
                        context = context,
                        channelId = channel.id,
                        userId = notAnAdmin.id,
                        input = UpdateMemberInput(role = OptionalChannelMemberRole(ChannelMemberRole.admin)),
                    )
                }

                error shouldBe ClientApiException.resourceNotFound()
                getMembers().first().role shouldBe notAnAdminMember.role
            }
        }

        @Test
        fun `allows an admin to degrade himself, when another admin exists`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin1) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin1!!.id, role = ChannelMemberRole.admin)
                )

                val (_, admin2) = upsertUser()
                addMember(
                    channelId = channel.id,
                    mockedChannelMemberWrite(userId = admin2!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin1.id)
                channelMutation.updateMember(
                    context = context,
                    channelId = channel.id,
                    userId = admin1.id,
                    input = UpdateMemberInput(role = OptionalChannelMemberRole(ChannelMemberRole.user)),
                )

                val actualRoles = getMembers().map { it.userId to it.role }
                val expectedRoles = listOf(admin1.id to ChannelMemberRole.user, admin2.id to ChannelMemberRole.admin)
                actualRoles shouldContainExactlyInAnyOrder expectedRoles
            }
        }

        @Test
        fun `returns an error when an admin degrades his role but no other admin exists`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin) = upsertUser()
                val (adminMember, _) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.updateMember(
                        context = context,
                        channelId = channel.id,
                        userId = admin.id,
                        input = UpdateMemberInput(role = OptionalChannelMemberRole(ChannelMemberRole.user)),
                    )
                }

                error shouldBe ClientApiException.channelMustHaveOneAdmin()
                getMembers().first().role shouldBe adminMember.role
            }
        }
    }

    @Nested
    inner class RemoveMemberTests {
        @Test
        fun `removes a member when the executing user is an admin of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, otherChannel) = createChannel()
                val (_, admin) = upsertUser()
                val (_, otherAdmin) = upsertUser()
                val (_, member1) = addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
                )
                addMember(
                    channelId = channel.id,
                    mockedChannelMemberWrite(userId = otherAdmin!!.id, role = ChannelMemberRole.admin)
                )
                // Have the same member as a member of another channel to see that only the specified one gets removed.
                val (_, member2) = addMember(
                    channelId = otherChannel!!.id,
                    mockedChannelMemberWrite(userId = otherAdmin.id, role = ChannelMemberRole.admin)
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
        fun `removes a member when the executing user is the same as the removed member and another admin exists`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin1) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin1!!.id, role = ChannelMemberRole.admin)
                )

                val (_, admin2) = upsertUser()
                addMember(
                    channelId = channel.id,
                    mockedChannelMemberWrite(userId = admin2!!.id, role = ChannelMemberRole.admin)
                )

                val context = mockedAuthContext(userId = admin1.id)
                channelMutation.removeMember(
                    context = context,
                    channelId = channel.id,
                    userId = admin1.id
                )

                getMembers().map { it.userId } shouldContainExactlyInAnyOrder listOf(admin2.id)
            }
        }

        @Test
        fun `returns an error when an admin removes himself and no other admin exists`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, admin) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = admin!!.id, role = ChannelMemberRole.admin)
                )

                val (_, user) = upsertUser()
                addMember(
                    channelId = channel.id,
                    mockedChannelMemberWrite(userId = user!!.id, role = ChannelMemberRole.user)
                )

                val context = mockedAuthContext(userId = admin.id)
                val error = shouldThrow<ClientApiException> {
                    channelMutation.removeMember(
                        context = context,
                        channelId = channel.id,
                        userId = admin.id
                    )
                }

                error shouldBe ClientApiException.channelMustHaveOneAdmin()
                getMembers() shouldHaveSize 2
            }
        }

        @Test
        fun `returns an error when the user is not an admin or the removed member`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()
                addMember(
                    channelId = channel!!.id,
                    mockedChannelMemberWrite(userId = user1!!.id, role = ChannelMemberRole.user)
                )
                addMember(
                    channelId = channel.id,
                    mockedChannelMemberWrite(userId = user2!!.id, role = ChannelMemberRole.user)
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
                val (_, user) = upsertUser()
                addMember(channelId = channel!!.id, mockedChannelMemberWrite(userId = user!!.id))

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
