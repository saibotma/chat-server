package platformapi.authentication.accesstoken

import io.ktor.server.auth.*

fun AuthenticationConfig.installPlatformApiAccessTokenAuthentication(accessToken: String) {
    val config = PlatformApiAccessTokenAuthenticationProvider.Configuration(accessToken = accessToken)
    val provider = PlatformApiAccessTokenAuthenticationProvider(config)
    register(provider)
}
