package platformapi

import dev.saibotma.persistence.postgres.jooq.tables.pojos.User
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.deleteUser
import persistence.postgres.queries.insertUser

suspend fun PipelineContext<Unit, ApplicationCall>.insertUser(
    location: UserList.UserDetails,
    database: KotlinDslContext
) {
    val user = call.receive<User>()
    database.transaction { insertUser(user) }
    call.respond(HttpStatusCode.Created)
}

suspend fun PipelineContext<Unit, ApplicationCall>.deleteUser(
    location: UserList.UserDetails,
    database: KotlinDslContext
) {
    database.transaction { deleteUser(location.userId) }
    call.respond(HttpStatusCode.OK)
}

