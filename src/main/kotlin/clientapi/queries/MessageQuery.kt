package clientapi.queries

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.models.DetailedMessageReadPayload
import clientapi.resourceNotFound
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getMessagesOf
import persistence.postgres.queries.isMemberOfChannel
import java.util.*

class MessageQuery(private val database: KotlinDslContext) {
    suspend fun messages(context: AuthContext, channelId: UUID): List<DetailedMessageReadPayload> {
        val userId = context.userId
        return database.transaction {
            if (!isMemberOfChannel(channelId = channelId, userId = userId)) throw ClientApiException.resourceNotFound()
            getMessagesOf(channelId = channelId, userId = context.userId)
        }
    }
}
