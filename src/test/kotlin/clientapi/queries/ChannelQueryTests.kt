package clientapi.queries

import clientapi.models.DetailedMessageReadPayload
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import models.*
import org.jooq.JSON
import org.jooq.impl.DSL
import org.jooq.impl.DSL.value
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import persistence.jooq.jsonArrayAggNoNull
import persistence.jooq.nowInstant
import persistence.jooq.tables.references.CHANNEL
import persistence.postgres.queries.selectMessagesOf
import testutil.mockedAuthContext
import testutil.mockedChannelMember
import testutil.mockedMessage
import testutil.serverTest
import java.time.Instant
import java.time.Instant.now

class ChannelQueryTests {
    @Nested
    inner class ChannelsTest {
        @Test
        fun `returns all channels of a user including latest messages`() {
            serverTest {
                val (_, channel1) = createChannel()
                val (_, channel2) = createChannel()
                val (_, user1) = upsertUser()
                val (_, user2) = upsertUser()

                val (_, member1) = addMember(channelId = channel1!!.id, mockedChannelMember(userId = user1!!.id))
                val (_, member2) = addMember(channelId = channel1.id, mockedChannelMember(userId = user2!!.id))
                val (_, member3) = addMember(channelId = channel2!!.id, mockedChannelMember(userId = user1.id))

                val context = mockedAuthContext(user1.id)
                val message =
                    messageMutation.sendMessage(context = context, channelId = channel1.id, message = mockedMessage())
                val channels = channelQuery.channels(context = context)

                channels.shouldContainExactlyInAnyOrder(
                    listOf(
                        channel1.toDetailed(
                            members = listOf(
                                member1!!.toDetailed(user = user1.toDetailed()),
                                member2!!.toDetailed(user = user2.toDetailed())
                            ),
                            messages = listOf(message),
                        ),
                        channel2.toDetailed(members = listOf(member3!!.toDetailed(user = user1.toDetailed())))
                    )
                )
            }
        }
    }
}

private fun ChannelReadPayload.toDetailed(
    members: List<DetailedChannelMemberReadPayload>,
    messages: List<DetailedMessageReadPayload> = emptyList(),
): DetailedChannelReadPayload {
    return DetailedChannelReadPayload(
        id = id,
        name = name,
        isManaged = isManaged,
        members = members,
        messages = messages,
        createdAt = createdAt,
    )
}

private fun ChannelMemberReadPayload.toDetailed(user: DetailedUserReadPayload): DetailedChannelMemberReadPayload {
    return DetailedChannelMemberReadPayload(
        channelId = channelId,
        userId = userId,
        user = user,
        role = role,
        addedAt = addedAt
    )
}
