package di

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseOptions
import io.ktor.config.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import push.FirebaseInitializer
import push.PushNotificationSender
import push.PushService
import util.firebaseCredentials
import java.nio.charset.StandardCharsets

val pushDi = DI.Module("push") {
    bind<PushNotificationSender>() with singleton { PushNotificationSender(instance()) }
    bind<PushService>() with singleton { PushService(instance(), instance()) }

    bind<FirebaseOptions>() with singleton {
        val config: HoconApplicationConfig by di.instance()
        val credentials = GoogleCredentials.fromStream(
            config.firebaseCredentials.trimIndent().byteInputStream(StandardCharsets.UTF_8)
        )

        FirebaseOptions.Builder().setCredentials(credentials).build()
    }
    bind<FirebaseInitializer>() with singleton { FirebaseInitializer(instance()) }
}
