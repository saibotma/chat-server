package kodein

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jackson.DoNotIgnoreIs
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val jacksonKodein = DI.Module("jackson") {
    bind<ObjectMapper>() with singleton {
        val config: ObjectMapper.() -> Unit = instance()
        jacksonObjectMapper().apply(config)
    }

    bind<ObjectMapper.() -> Unit>() with singleton {
        {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            propertyNamingStrategy = DoNotIgnoreIs
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
}
