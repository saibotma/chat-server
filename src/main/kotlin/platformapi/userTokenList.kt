package platformapi

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import error.PlatformApiException
import error.resourceNotFound
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import models.UserToken
import persistence.postgres.queries.getUser
import java.time.Instant.now
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.createUserToken(
    location: UserList.UserDetails.UserTokenList,
    database: KotlinDslContext,
    jwtSecret: String,
) {
    val userId = location.userDetails.userId
    val userDoesNotExist = database.transaction { getUser(userId) == null }
    if (userDoesNotExist) throw PlatformApiException.resourceNotFound()
    val jwt = JWT.create()
        .withSubject(userId)
        .withExpiresAt(Date.from(now().plusSeconds(60 * 15)))
        .sign(Algorithm.HMAC256(jwtSecret))
    call.respond(HttpStatusCode.Created, UserToken(jwt = jwt))
}
