package clientapi.queries

import clientapi.AuthContext
import clientapi.models.UserEventReadPayload
import clientapi.models.toReadPayload
import com.fasterxml.jackson.databind.ObjectMapper
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.userevent.getUserEvents

class UserEventQuery(val database: KotlinDslContext, val objectMapper: ObjectMapper) {
    suspend fun userEvents(context: AuthContext, beforeId: Long, take: Int): List<UserEventReadPayload> {
        val rawEvents =
            database.transaction { getUserEvents(userId = context.userId, beforeId = beforeId, take = take) }
        return rawEvents.map { it.toReadPayload(objectMapper = objectMapper) }
    }
}
