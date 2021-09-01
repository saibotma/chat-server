package di

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseOptions
import io.ktor.config.*
import io.ktor.util.*
import org.kodein.di.*
import push.FirebaseInitializer
import push.PushNotificationSender
import push.PushService
import util.firebaseCredentials
import java.nio.charset.StandardCharsets
import java.util.*

val pushDi = DI.Module("push") {
    bind<Optional<PushNotificationSender>>() with singleton {
        val firebaseInitializer: Optional<FirebaseInitializer> by di.instance()
        if (firebaseInitializer.isEmpty) return@singleton Optional.empty<PushNotificationSender>()
        Optional.of(PushNotificationSender(instance()))
    }
    bind<Optional<PushService>>() with singleton {
        val pushNotificationSender: Optional<PushNotificationSender> by di.instance()
        if (pushNotificationSender.isEmpty) return@singleton Optional.empty<PushService>()
        Optional.of(PushService(instance(), pushNotificationSender.get()))
    }

    bind<Optional<FirebaseInitializer>>() with singleton {
        val config: HoconApplicationConfig by di.instance()
        val firebaseCredentials = config.firebaseCredentials ?: return@singleton Optional.empty<FirebaseInitializer>()

        val credentials = GoogleCredentials.fromStream(
            String(Base64.getDecoder().decode(firebaseCredentials))
                .trimIndent()
                .byteInputStream(StandardCharsets.UTF_8)
        )

        val options = FirebaseOptions.Builder().setCredentials(credentials).build()
        Optional.of(FirebaseInitializer(options))
    }
}
