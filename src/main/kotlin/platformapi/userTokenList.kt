package platformapi

import clientapi.UserId
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import error.PlatformApiException
import error.resourceNotFound
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import models.UserToken
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getUser
import java.time.Instant.now
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.createUserToken(
    location: UserList.UserDetails.UserTokenList,
    database: KotlinDslContext,
    jwtSecret: String,
) {
    val userId = UserId(location.userDetails.userId)
    val userDoesNotExist = database.transaction { getUser(userId) == null }
    if (userDoesNotExist) throw PlatformApiException.resourceNotFound()
    val jwt = JWT.create()
        .withSubject(userId.value)
        .withExpiresAt(Date.from(now().plusSeconds(60 * 15)))
        .sign(Algorithm.HMAC256(jwtSecret))
    call.respond(HttpStatusCode.Created, UserToken(jwt = jwt))
}
