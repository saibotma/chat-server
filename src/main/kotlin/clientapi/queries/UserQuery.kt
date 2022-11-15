package clientapi.queries

import clientapi.AuthContext
import clientapi.ClientApiException
import clientapi.resourceNotFound
import models.DetailedUserReadPayload
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.channelmember.areMembersOfSameChannel
import persistence.postgres.queries.user.getContactsOf
import persistence.postgres.queries.user.getDetailedUser

class UserQuery(private val database: KotlinDslContext) {
    suspend fun contacts(context: AuthContext): List<DetailedUserReadPayload> {
        return database.transaction { getContactsOf(context.userId) }
    }

    suspend fun user(context: AuthContext, userId: String): DetailedUserReadPayload {
        val areMembersOfSameChannel =
            database.transaction { areMembersOfSameChannel(userId1 = context.userId, userId2 = userId) }
        if (!areMembersOfSameChannel) throw ClientApiException.resourceNotFound()

        return database.transaction { getDetailedUser(userId) } ?: throw ClientApiException.resourceNotFound()
    }
}
