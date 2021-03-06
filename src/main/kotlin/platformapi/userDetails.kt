package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.deleteUser
import persistence.postgres.queries.getUser
import persistence.postgres.queries.upsertUser
import models.UserWritePayload
import models.toUser
import models.toUserRead
import java.time.Instant.now

suspend fun PipelineContext<Unit, ApplicationCall>.upsertUser(
    location: UserList.UserDetails,
    database: KotlinDslContext
) {
    val userId = location.userId
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
    database.transaction { deleteUser(location.userId) }
    call.respond(HttpStatusCode.NoContent)
}

