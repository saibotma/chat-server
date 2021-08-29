package testutil

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import persistence.postgres.PostgresConfig
import push.FirebaseInitializer
import push.PushNotificationSender

fun DI.MainBuilder.setupTestDependencies() {
    bind<FirebaseInitializer>(overrides = true) with singleton { mockedFirebaseInitializer() }
    bind<PushNotificationSender>(overrides = true) with singleton { mockedPushNotificationSender() }
}
