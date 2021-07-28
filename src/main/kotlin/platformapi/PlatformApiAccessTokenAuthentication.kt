package platformapi

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

private const val configurationName = "PlatformApiAccessTokenAuthentication"

class PlatformApiAccessTokenAuthenticationProvider(config: Configuration) : AuthenticationProvider(config) {
    class Configuration : AuthenticationProvider.Configuration(name = configurationName)
}

fun Route.platformApiAccessTokenAuthenticate(build: Route.() -> Unit) {
    authenticate(configurationName, build = build)
}

fun Authentication.Configuration.installPlatformApiAccessTokenAuthentication(expectedAccessToken: String) {
    val config = PlatformApiAccessTokenAuthenticationProvider.Configuration()
    val provider = PlatformApiAccessTokenAuthenticationProvider(config)

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val actualAccessToken = call.request.headers["X-Chat-Server-Platform-Api-Access-Token"]
        val validatedApiKey = if (actualAccessToken == expectedAccessToken) actualAccessToken else null
        val cause = when {
            actualAccessToken == null -> AuthenticationFailedCause.NoCredentials
            validatedApiKey == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(configurationName, cause) {
                call.respond(HttpStatusCode.Unauthorized)
                it.complete()
            }
        }
    }

    register(provider)
}


