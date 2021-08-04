package clientapi.queries

import clientapi.AuthContext
import clientapi.models.DetailedMessage
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getMessagesOf
import java.util.*

class MessageQuery(private val database: KotlinDslContext) {
    suspend fun messages(context: AuthContext, channelId: UUID): List<DetailedMessage> {
        return database.transaction {
            getMessagesOf(channelId = channelId, userId = context.userId)
        }
    }
}
