package clientapi

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import models.ChannelReadPayload
import models.DetailedChannelReadPayload
import models.DetailedUserReadPayload
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import platformapi.models.ChannelMemberReadPayload
import platformapi.models.DetailedChannelMemberReadPayload
import models.UserReadPayload
import testutil.mockedAuthContext
import testutil.mockedChannelMember
import testutil.serverTest

class ChannelQueryTests {
    @Nested
    inner class ChannelsTest {
        @Test
        fun `returns all channels of a user`() {
            serverTest {
                val (_, channel1) = createChannel()
                val (_, channel2) = createChannel()
                val (_, user1) = createUser()
                val (_, user2) = createUser()

                val (_, member1) = addMember(channelId = channel1!!.id, mockedChannelMember(userId = user1!!.id))
                val (_, member2) = addMember(channelId = channel1.id, mockedChannelMember(userId = user2!!.id))
                val (_, member3) = addMember(channelId = channel2!!.id, mockedChannelMember(userId = user1.id))

                val context = mockedAuthContext(user1.id)
                val channels = channelQuery.channels(context = context)

                channels.shouldContainExactlyInAnyOrder(
                    listOf(
                        channel1.toDetailed(
                            members = listOf(
                                member1!!.toDetailed(user = user1.toDetailed()),
                                member2!!.toDetailed(user = user2.toDetailed())
                            )
                        ),
                        channel2.toDetailed(members = listOf(member3!!.toDetailed(user = user1.toDetailed())))
                    )
                )
            }
        }
    }
}

private fun ChannelReadPayload.toDetailed(members: List<DetailedChannelMemberReadPayload>): DetailedChannelReadPayload {
    return DetailedChannelReadPayload(
        id = id,
        name = name,
        isManaged = isManaged,
        members = members,
        createdAt = createdAt,
    )
}

private fun UserReadPayload.toDetailed(): DetailedUserReadPayload {
    return DetailedUserReadPayload(
        id = id,
        name = name,
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
