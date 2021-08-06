package clientapi.mutations

import clientapi.ClientApiException
import clientapi.models.MessageWritePayload
import clientapi.resourceNotFound
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Message
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
                val otherMessage =
                    messageMutation.sendMessage(context, channelId = channel.id, message = mockedMessage())
                val message = mockedMessage(text = "Message 1", respondedMessageId = otherMessage.id)
                val detailedMessage = messageMutation.sendMessage(
                    context = context,
                    channelId = channel.id,
                    message = message
                )

                with(getMessages()) {
                    shouldHaveSize(1)
                    detailedMessage.id shouldBe first().id
                    first().toMessageWrite() shouldBe message
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
    }
}

private fun Message.toMessageWrite(): MessageWritePayload {
    return MessageWritePayload(text = text, respondedMessageId = respondedMessageId)
}
