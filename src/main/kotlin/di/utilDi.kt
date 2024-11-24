package di

import com.fasterxml.jackson.databind.ObjectMapper
import graphqlclientapi.ClientApiConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import org.kodein.di.*
import platformapi.PlatformApiConfig
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

    bind<HttpClient>() with singleton {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson { di.direct.instance<ObjectMapper.() -> Unit>()() }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10 * 1000
                connectTimeoutMillis = 10 * 1000
            }
        }
    }
}
