package clientapi.queries

import clientapi.AuthContext
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.userevent.getUserEvents

class UserEventQuery(val database: KotlinDslContext) {
    suspend fun userEvents(context: AuthContext, beforeId: Long, take: Int) {
        return database.transaction { getUserEvents(userId = context.userId, beforeId = beforeId, take = take) }
    }
}
