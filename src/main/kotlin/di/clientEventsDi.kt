package di

import clientapi.TargetedMessageSessionManager
import com.fasterxml.jackson.databind.ObjectMapper
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val clientEventsDi = DI.Module("clientEventsDi") {
    bind<TargetedMessageSessionManager>() with singleton {
        val objectMapper: ObjectMapper = instance()
        TargetedMessageSessionManager(objectMapper)
    }
}
