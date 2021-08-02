package di

import clientapi.ClientApiConfig
import platformapi.PlatformApiConfig
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import util.clientApiJwtSecret
import util.platformApiAccessToken

val utilDi = DI.Module("util") {
    bind<HoconApplicationConfig>() with singleton { HoconApplicationConfig(ConfigFactory.load()) }
    bind<PlatformApiConfig>() with singleton {
        val config: HoconApplicationConfig = instance()
        PlatformApiConfig(accessToken = config.platformApiAccessToken)
    }
    bind<ClientApiConfig>() with singleton {
        val config: HoconApplicationConfig = instance()
        ClientApiConfig(jwtSecret = config.clientApiJwtSecret)
    }
}
