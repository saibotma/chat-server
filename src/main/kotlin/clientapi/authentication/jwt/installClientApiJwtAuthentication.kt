package clientapi.authentication.jwt

import clientapi.AuthContext
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getUser

const val clientApiJwtAuthentication = "clientApiJwtAuthentication"

fun AuthenticationConfig.installClientApiJwtAuthentication(
    jwtSecret: String,
    kotlinDslContext: KotlinDslContext,
) {
    jwt(clientApiJwtAuthentication) {
        verifier(
            JWT
                .require(Algorithm.HMAC256(jwtSecret))
                .build()
        )
        validate { credential ->
            val user = kotlinDslContext.transaction { getUser(credential.payload.subject) } ?: return@validate null
            AuthContext(userId = user.id!!)
        }
    }
}
