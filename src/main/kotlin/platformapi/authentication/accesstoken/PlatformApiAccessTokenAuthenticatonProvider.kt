package platformapi.authentication.accesstoken

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val platformApiAccessTokenAuthentication = "platformApiAccessTokenAuthentication"


object PlatformApiAccessTokenPrincipal : Principal

class PlatformApiAccessTokenAuthenticationProvider(config: Configuration) : AuthenticationProvider(config) {
    private val accessToken = config.accessToken

    class Configuration(val accessToken: String) : Config(name = platformApiAccessTokenAuthentication)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        val actualAccessToken = call.request.headers["X-Chat-Server-Platform-Api-Access-Token"]
        val cause = when {
            actualAccessToken == null -> AuthenticationFailedCause.NoCredentials
            actualAccessToken != accessToken -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            @Suppress("NAME_SHADOWING")
            context.challenge(platformApiAccessTokenAuthentication, cause) { challenge, call ->
                call.respond(HttpStatusCode.Unauthorized)
                challenge.complete()
            }
            return
        }

        context.principal(PlatformApiAccessTokenPrincipal)
    }
}
