package clientapi.mutations

import clientapi.AuthContext
import dev.saibotma.persistence.postgres.jooq.tables.pojos.Channel
import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getChannelsOf
import persistence.postgres.queries.insertChannel
import persistence.postgres.queries.insertMembers
import java.time.Instant.now
import java.util.UUID.randomUUID

class ChannelMutation(
    private val database: KotlinDslContext,
) {
    suspend fun addChannel(
        context: AuthContext,
        name: String?,
        memberUserIds: List<String>
    ): clientapi.models.DetailedChannel {
        return database.transaction {
            val id = randomUUID()
            insertChannel(Channel(id = id, name = name, isManaged = false, createdAt = now()))
            insertMembers(memberUserIds.map { ChannelMember(channelId = id, userId = it) })
            getChannelsOf(userId = context.userId, channelIdFilter = id).first()
        }
    }
}
