package clientapi.mutations

import clientapi.ClientApiException
import clientapi.models.MessageWritePayload
import clientapi.resourceNotFound
import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Message
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testutil.mockedAuthContext
import testutil.mockedChannelMember
import testutil.mockedMessage
import testutil.serverTest

class MessageMutationTests {
    @Nested
    inner class SendMessageTests {
        @Test
        fun `sends a message and returns it`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user) = createUser()
                addMember(channelId = channel!!.id, member = mockedChannelMember(userId = user!!.id))

                val context = mockedAuthContext(userId = user.id)
                val otherMessageWrite = mockedMessage()
                val otherMessage =
                    messageMutation.sendMessage(context, channelId = channel.id, message = otherMessageWrite)
                val message = mockedMessage(text = "Message 1", respondedMessageId = otherMessage.id)
                val detailedMessage = messageMutation.sendMessage(
                    context = context,
                    channelId = channel.id,
                    message = message
                )

                getMessages().map { it.toMessageWrite() }
                    .shouldContainExactlyInAnyOrder(listOf(otherMessageWrite, message))
                detailedMessage.repliedMessageId shouldNotBe detailedMessage.id
            }
        }
    }

    @Test
    fun `returns an error when the user is not a member of the channel`() {
        serverTest {
            val (_, channel) = createChannel()
            val (_, user) = createUser()

            val context = mockedAuthContext(userId = user!!.id)
            val error = shouldThrow<ClientApiException> {
                messageMutation.sendMessage(
                    context = context,
                    channelId = channel!!.id,
                    message = mockedMessage()
                )
            }

            error shouldBe ClientApiException.resourceNotFound()
            getMessages() shouldHaveSize 0
        }
    }

    @Nested
    inner class EditMessageTests {
        @Test
        fun `edits a message and returns it`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user) = createUser()
                addMember(channelId = channel!!.id, member = mockedChannelMember(userId = user!!.id))

                val context = mockedAuthContext(userId = user.id)
                val otherMessageWrite = mockedMessage(text = "Message 1")
                messageMutation.sendMessage(
                    context,
                    channelId = channel.id,
                    message = otherMessageWrite
                )
                val messageWrite = mockedMessage(text = "Message 2")
                val detailedMessage = messageMutation.sendMessage(
                    context = context,
                    channelId = channel.id,
                    message = messageWrite
                )

                val updatedDetailedMessage =
                    messageMutation.editMessage(context, id = detailedMessage.id, text = "Updated message 2")

                updatedDetailedMessage.id shouldBe detailedMessage.id
                getMessages().map { it.toMessageWrite() }
                    .shouldContainExactlyInAnyOrder(
                        listOf(
                            otherMessageWrite,
                            messageWrite.copy(text = "Updated message 2")
                        )
                    )
            }
        }

        @Test
        fun `returns an error when the user is not the creator of the message`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user1) = createUser()
                val (_, user2) = createUser()
                // Give him admin permissions to make sure that also not admins can edit messages of others.
                addMember(
                    channelId = channel!!.id,
                    member = mockedChannelMember(userId = user1!!.id, role = ChannelMemberRole.admin)
                )
                addMember(channelId = channel.id, member = mockedChannelMember(userId = user2!!.id))

                val context1 = mockedAuthContext(userId = user1.id)
                messageMutation.sendMessage(
                    context1,
                    channelId = channel.id,
                    message = mockedMessage()
                )
                val context2 = mockedAuthContext(userId = user2.id)
                val message = messageMutation.sendMessage(
                    context2,
                    channelId = channel.id,
                    message = mockedMessage()
                )

                val error = shouldThrow<ClientApiException> {
                    messageMutation.editMessage(context1, id = message.id, text = "Updated message 2")
                }

                error shouldBe ClientApiException.resourceNotFound()
                getMessages().map { it.text } shouldNotContain "Updated message 2"
            }
        }
    }

    @Nested
    inner class DeleteMessageTests {
        @Test
        fun `deletes a message`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user) = createUser()
                addMember(channelId = channel!!.id, member = mockedChannelMember(userId = user!!.id))

                val context = mockedAuthContext(userId = user.id)
                val otherMessage = messageMutation.sendMessage(
                    context,
                    channelId = channel.id,
                    message = mockedMessage()
                )
                val message = messageMutation.sendMessage(
                    context = context,
                    channelId = channel.id,
                    message = mockedMessage()
                )

                messageMutation.deleteMessage(context, id = message.id)

                with(getMessages()) {
                    shouldHaveSize(1)
                    first().id shouldBe otherMessage.id
                }
            }
        }

        @Test
        fun `returns an error when the user is not the creator of the message`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user1) = createUser()
                val (_, user2) = createUser()
                // Give him admin permissions to make sure that also not admins can edit messages of others.
                addMember(
                    channelId = channel!!.id,
                    member = mockedChannelMember(userId = user1!!.id, role = ChannelMemberRole.admin)
                )
                addMember(channelId = channel.id, member = mockedChannelMember(userId = user2!!.id))

                val context1 = mockedAuthContext(userId = user1.id)
                val context2 = mockedAuthContext(userId = user2.id)
                val message = messageMutation.sendMessage(
                    context2,
                    channelId = channel.id,
                    message = mockedMessage()
                )

                val error = shouldThrow<ClientApiException> {
                    messageMutation.deleteMessage(context1, id = message.id)
                }

                error shouldBe ClientApiException.resourceNotFound()
                getMessages() shouldHaveSize 1
            }
        }
    }
}


private fun Message.toMessageWrite(): MessageWritePayload {
    return MessageWritePayload(text = text, repliedMessageId = repliedMessageId)
}
