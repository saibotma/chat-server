package testutil

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import push.FirebaseInitializer
import push.PushNotificationSender
import java.util.*

fun DI.MainBuilder.setupTestDependencies() {
    bind<Optional<FirebaseInitializer>>(overrides = true) with singleton { Optional.of(mockedFirebaseInitializer()) }
    bind<Optional<PushNotificationSender>>(overrides = true) with singleton { Optional.of(mockedPushNotificationSender()) }
}
