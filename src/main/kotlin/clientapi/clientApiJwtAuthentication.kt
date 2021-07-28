package clientapi

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.saibotma.persistence.postgres.jooq.tables.pojos.User
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.routing.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getUser

private const val configurationName = "ClientApiJwtAuthentication"

fun Route.clientApiJwtAuthenticate(build: Route.() -> Unit) {
    authenticate(configurationName, build = build)
}

fun Authentication.Configuration.installClientApiJwtAuthentication(
    jwtSecret: String,
    kotlinDslContext: KotlinDslContext,
) {
    jwt("clientApiJwtAuthentication") {
        verifier(
            JWT
                .require(Algorithm.HMAC256(jwtSecret))
                .build()
        )
        validate { credential ->
            val user = kotlinDslContext.transaction { getUser(credential.payload.subject) } ?: return@validate null
            ClientApiJwtAuthenticationPrinciple(user = user)
        }
    }
}

data class ClientApiJwtAuthenticationPrinciple(val user: User) : Principal
