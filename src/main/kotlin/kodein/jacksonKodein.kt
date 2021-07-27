package app.storyways.kodein

import app.storyways.jackson.DoNotIgnoreIs
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.kodein.di.Kodein.Module
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

val jacksonKodein = Module("jackson") {

    bind<ObjectMapper>() with singleton {
        val config = instance<ObjectMapper.() -> Unit>()
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
