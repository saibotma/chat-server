package clientapi.mutations

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.resourceNotFound
import persistence.jooq.tables.pojos.Channel
import models.DetailedChannelReadPayload
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import models.ChannelMemberWritePayload
import models.DetailedChannelMemberReadPayload
import models.toChannelMember
import java.time.Instant.now
import java.util.*
import java.util.UUID.randomUUID

class ChannelMutation(
    private val database: KotlinDslContext,
) {
    suspend fun createChannel(
        context: AuthContext,
        name: String?,
        members: List<ChannelMemberWritePayload>
    ): DetailedChannelReadPayload {
        return database.transaction {
            val id = randomUUID()
            insertChannel(Channel(id = id, name = name, isManaged = false, createdAt = now()))
            insertMembers(members.map { it.toChannelMember(channelId = id, addedAt = now()) })
            getChannelsOf(userId = context.userId, channelIdFilter = id).first()
        }
    }

    suspend fun updateChannel(
        context: AuthContext,
        id: UUID,
        name: String?
    ): DetailedChannelReadPayload {
        val userId = context.userId
        return database.transaction {
            if (!isAdminOfChannel(channelId = id, userId = userId)) throw ClientApiException.resourceNotFound()
            updateChannel(id = id, name = name)
            getChannelsOf(userId, channelIdFilter = id).first()
        }
    }

    suspend fun deleteChannel(
        context: AuthContext,
        id: UUID
    ): Boolean {
        val userId = context.userId
        database.transaction {
            if (!isAdminOfChannel(channelId = id, userId = userId)) throw ClientApiException.resourceNotFound()
            deleteChannel(id)
        }
        return true
    }

    suspend fun upsertMember(
        context: AuthContext,
        channelId: UUID,
        member: ChannelMemberWritePayload,
    ): DetailedChannelMemberReadPayload {
        val userId = context.userId
        return database.transaction {
            if (!isAdminOfChannel(channelId = channelId, userId = userId)) {
                throw ClientApiException.resourceNotFound()
            }
            upsertMember(channelId = channelId, userId = member.userId, role = member.role)
            getDetailedMember(channelId = channelId, userId = member.userId)!!
        }
    }

    suspend fun removeMember(
        context: AuthContext,
        channelId: UUID,
        userId: String,
    ): Boolean {
        database.transaction {
            val channel = getChannel(channelId = channelId) ?: throw ClientApiException.resourceNotFound()
            if ((!isAdminOfChannel(channelId = channelId, userId = userId) && userId != context.userId)
                || channel.isManaged!!
            ) {
                throw ClientApiException.resourceNotFound()
            }
            deleteMember(channelId = channelId, userId = userId)
        }
        return true
    }
}
