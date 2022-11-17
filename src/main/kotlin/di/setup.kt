package di

import io.ktor.server.config.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

fun DI.MainBuilder.setupDi(config: ApplicationConfig) {
    bind<ApplicationConfig>() with singleton { config }
    import(jacksonDi)
    import(utilDi)
    import(postgresDi)
    import(graphQlDi)
    import(pushDi)
    import(clientEventsDi)
}
