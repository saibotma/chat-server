package clientapi.mutations

import clientapi.AuthContext
import clientapi.models.DetailedChannel
import dev.saibotma.persistence.postgres.jooq.enums.ChannelMemberRole
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Channel
import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import models.DetailedChannelMember
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import java.time.Instant.now
import java.util.*
import java.util.UUID.randomUUID

class ChannelMutation(
    private val database: KotlinDslContext,
) {
    suspend fun addChannel(
        context: AuthContext,
        name: String?,
        memberUserIds: List<String>
    ): DetailedChannel {
        return database.transaction {
            val id = randomUUID()
            insertChannel(Channel(id = id, name = name, isManaged = false, createdAt = now()))
            insertMembers(memberUserIds.map { ChannelMember(channelId = id, userId = it) })
            getChannelsOf(userId = context.userId, channelIdFilter = id).first()
        }
    }

    suspend fun updateChannel(
        context: AuthContext,
        id: UUID,
        name: String?
    ): DetailedChannel {
        return database.transaction {
            updateChannel(id = id, name = name)
            getChannelsOf(context.userId, channelIdFilter = id).first()
        }
    }

    suspend fun deleteChannel(
        context: AuthContext,
        id: UUID
    ) {
        database.transaction {
            deleteChannel(id)
        }
    }

    suspend fun upsertMember(
        context: AuthContext,
        channelId: UUID,
        userId: String,
        role: ChannelMemberRole
    ): DetailedChannelMember {
        return database.transaction {
            upsertMember(channelId = channelId, userId = userId, role = role)
            getDetailedMember(channelId = channelId, userId = userId)!!
        }
    }

    suspend fun deleteMember(
        context: AuthContext,
        channelId: UUID,
        userId: String,
    ) {
        database.transaction {
            deleteMember(channelId = channelId, userId = userId)
        }
    }
}
