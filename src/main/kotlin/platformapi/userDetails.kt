package platformapi

import clientapi.UserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import models.UserWritePayload
import models.toUser
import models.toUserRead
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.deleteUser
import persistence.postgres.queries.getUser
import persistence.postgres.queries.upsertUser
import java.time.Instant.now

suspend fun PipelineContext<Unit, ApplicationCall>.upsertUser(
    location: UserList.UserDetails,
    database: KotlinDslContext
) {
    val userId = UserId(location.userId)
    val user = call.receive<UserWritePayload>()
    val result = database.transaction {
        upsertUser(user.toUser(id = userId, createdAt = now()))
        getUser(userId)
    }
    call.respond(HttpStatusCode.Created, result!!.toUserRead())
}

// TODO(saibotma): Implement updating a user https://github.com/saibotma/chat-server/issues/3

suspend fun PipelineContext<Unit, ApplicationCall>.deleteUser(
    location: UserList.UserDetails,
    database: KotlinDslContext
) {
    val userId = UserId(location.userId)
    database.transaction { deleteUser(userId) }
    call.respond(HttpStatusCode.NoContent)
}

