package clientapi.queries

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.models.DetailedMessageReadPayload
import clientapi.models.MessageWritePayload
import clientapi.resourceNotFound
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import models.DetailedUserReadPayload
import models.toDetailed
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testutil.mockedAuthContext
import testutil.mockedChannelMemberWrite
import testutil.mockedMessage
import testutil.servertest.post.addMember
import testutil.servertest.post.createChannel
import testutil.servertest.put.upsertUser
import testutil.servertest.serverTest
import java.time.Instant
import java.time.Instant.now
import java.util.*

class MessageQueryTests {
    @Nested
    inner class MessagesTests {
        @Test
        fun `returns messages by date time ascending`() {
            testReturnsMultipleMessages(queryByMessageId = false)
        }

        @Test
        fun `returns messages by message id ascending`() {
            testReturnsMultipleMessages(queryByMessageId = true)
        }

        @Test
        fun `returns the latest messages`() {
            testReturnsFirstOrLatest(shouldReturnLatest = true)
        }

        @Test
        fun `returns the first messages`() {
            testReturnsFirstOrLatest(shouldReturnLatest = false)
        }

        @Test
        fun `only returns messages of the specified channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, otherChannel) = createChannel()
                val (_, user) = upsertUser()
                addMember(channelId = channel!!.id, mockedChannelMemberWrite(userId = user!!.id))
                addMember(channelId = otherChannel!!.id, mockedChannelMemberWrite(userId = user.id))

                val context = mockedAuthContext(userId = user.id)
                suspend fun sendMessage(text: String, channelId: UUID): DetailedMessageReadPayload {
                    return messageMutation.sendMessage(
                        context = context,
                        channelId = channelId,
                        message = mockedMessage(text = text)
                    )
                }

                val message = sendMessage("message", channel.id)
                sendMessage("other message", otherChannel.id)

                val detailedMessages = messageQuery.messages(
                    context = context,
                    channelId = channel.id,
                    byDateTime = null,
                    byMessageId = message.id,
                )

                with(detailedMessages) {
                    shouldHaveSize(1)
                    first().id shouldBe message.id
                }
            }
        }

        @Test
        fun `returns correct data`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()
                addMember(channelId = channel!!.id, mockedChannelMemberWrite(userId = user1!!.id))
                addMember(channelId = channel.id, mockedChannelMemberWrite(userId = user2!!.id))

                val context1 = mockedAuthContext(userId = user1.id)
                val context2 = mockedAuthContext(userId = user2.id)
                suspend fun AuthContext.sendMessage(message: MessageWritePayload): DetailedMessageReadPayload {
                    return messageMutation.sendMessage(
                        context = this,
                        channelId = channel.id,
                        message = message,
                    )
                }

                val messageWrite1 = mockedMessage(text = "message 1")
                val message1 = context1.sendMessage(messageWrite1)
                val messageWrite2 = mockedMessage(text = "message 2", respondedMessageId = message1.id)
                val message2 = context2.sendMessage(messageWrite2)

                val detailedMessages = messageQuery.messages(
                    context = context1,
                    channelId = channel.id,
                    byDateTime = null,
                    byMessageId = message1.id,
                )

                detailedMessages shouldContainExactly listOf(
                    messageWrite1.toDetailed(
                        id = message1.id,
                        creator = user1.toDetailed(),
                        createdAt = message1.createdAt,
                    ),
                    messageWrite2.toDetailed(
                        id = message2.id,
                        creator = user2.toDetailed(),
                        createdAt = message2.createdAt,
                    )
                )
            }
        }

        @Test
        fun `returns an error when the user is not a member of the channel`() {
            serverTest {
                val (_, channel) = createChannel()
                val (_, user) = upsertUser()
                val error = shouldThrow<ClientApiException> {
                    messageQuery.messages(
                        context = mockedAuthContext(user!!.id),
                        channelId = channel!!.id,
                        byDateTime = now(),
                        byMessageId = null,
                    )
                }
                error shouldBe ClientApiException.resourceNotFound()
            }
        }
    }

    private fun testReturnsMultipleMessages(queryByMessageId: Boolean) {
        serverTest {
            val (_, channel) = createChannel()
            val (_, user) = upsertUser()
            addMember(channelId = channel!!.id, mockedChannelMemberWrite(userId = user!!.id))

            val context = mockedAuthContext(userId = user.id)
            suspend fun sendMessage(text: String): DetailedMessageReadPayload {
                return messageMutation.sendMessage(
                    context = context,
                    channelId = channel.id,
                    message = mockedMessage(text = text)
                )
            }

            sendMessage("1")
            val message2 = sendMessage("2")
            val message3 = sendMessage("3")
            val message4 = sendMessage("4")
            sendMessage("5")

            val detailedMessages = messageQuery.messages(
                context = context,
                channelId = channel.id,
                byDateTime = if (queryByMessageId) null else message3.createdAt,
                byMessageId = if (queryByMessageId) message3.id else null,
                previousLimit = 1,
                nextLimit = 1,
            )

            val expectedDetailedMessages = listOf(message2, message3, message4)
            detailedMessages shouldContainExactly expectedDetailedMessages
        }
    }

    private fun testReturnsFirstOrLatest(shouldReturnLatest: Boolean) {
        serverTest {
            val (_, channel) = createChannel()
            val (_, user) = upsertUser()
            addMember(channelId = channel!!.id, mockedChannelMemberWrite(userId = user!!.id))

            val context = mockedAuthContext(userId = user.id)
            suspend fun sendMessage(text: String): DetailedMessageReadPayload {
                return messageMutation.sendMessage(
                    context = context,
                    channelId = channel.id,
                    message = mockedMessage(text = text)
                )
            }

            val message1 = sendMessage("1")
            val message2 = sendMessage("2")
            val message3 = sendMessage("3")

            val detailedMessages = messageQuery.messages(
                context = context,
                channelId = channel.id,
                byDateTime = null,
                byMessageId = null,
                previousLimit = if (shouldReturnLatest) 2 else 0,
                nextLimit = if (shouldReturnLatest) 0 else 2,
            )

            val expectedDetailedMessages =
                if (shouldReturnLatest) listOf(message2, message3) else listOf(message1, message2)
            detailedMessages shouldContainExactly expectedDetailedMessages
        }
    }
}

private fun MessageWritePayload.toDetailed(
    id: UUID,
    creator: DetailedUserReadPayload,
    createdAt: Instant
): DetailedMessageReadPayload {
    return DetailedMessageReadPayload(
        id = id,
        text = text,
        repliedMessageId = repliedMessageId,
        creator = creator,
        createdAt
    )
}
