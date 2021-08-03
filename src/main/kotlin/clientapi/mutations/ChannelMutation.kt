package clientapi.mutations

import clientapi.AuthContext
import dev.saibotma.persistence.postgres.jooq.tables.pojos.ChannelMember
import models.ChannelMeta
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.insertChannel
import persistence.postgres.queries.insertMembers
import java.util.*
import java.util.UUID.randomUUID

class ChannelMutation(
    private val database: KotlinDslContext,
) {
    suspend fun addChatRoom(context: AuthContext, name: String?, memberIds: List<String>): UUID {
        return database.transaction {
            val id = randomUUID()
            insertChannel(ChannelMeta(id = randomUUID(), name = name, isManaged = false))
            insertMembers(memberIds.map { ChannelMember(channelId = id, userId = it) })
            id
        }
    }
}
