package testutil

import di.buildFlywayConfiguration
import di.setupDi
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import push.FirebaseInitializer
import push.PushNotificationSender
import java.util.*

fun DI.MainBuilder.setupTestDependencies(config: ApplicationConfig = ApplicationConfig(configPath = null)) {
    setupDi(config)
    bind<Optional<FirebaseInitializer>>(overrides = true) with singleton { Optional.of(mockedFirebaseInitializer()) }
    bind<Optional<PushNotificationSender>>(overrides = true) with singleton { Optional.of(mockedPushNotificationSender()) }
    bind<Flyway>(overrides = true) with singleton {
        val configuration = buildFlywayConfiguration(dataSource = instance(), isCleanDisabled = false)
        Flyway(configuration)
    }
}
