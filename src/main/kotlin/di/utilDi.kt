package di

import clientapi.ClientApiConfig
import platformapi.PlatformApiConfig
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import util.clientApiJwtSecret
import util.platformApiAccessToken

val utilDi = DI.Module("util") {
    bind<PlatformApiConfig>() with singleton {
        val config: ApplicationConfig = instance()
        PlatformApiConfig(accessToken = config.platformApiAccessToken)
    }
    bind<ClientApiConfig>() with singleton {
        val config: ApplicationConfig = instance()
        ClientApiConfig(jwtSecret = config.clientApiJwtSecret)
    }
}
