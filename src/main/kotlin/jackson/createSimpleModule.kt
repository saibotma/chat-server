package jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.Instant
import java.time.OffsetDateTime

inline fun <reified T> createSimpleModule(
    crossinline deserialize: (value: String) -> T,
    crossinline serialize: (item: T) -> String
): SimpleModule {

    val serializer = object : StdSerializer<T>(T::class.java) {

        override fun serialize(value: T?, gen: JsonGenerator?, serializers: SerializerProvider?) {
            val jsonValue = value ?: throw IllegalStateException("Value of ${T::class.java} can not be null!")
            val generator = gen ?: throw IllegalStateException("JsonGenerator can not be null!")
            generator.writeString(serialize(jsonValue))
        }
    }

    val deserializer = object : StdDeserializer<T>(T::class.java) {
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): T {
            val json = p?.readValueAs(String::class.java) ?: throw IllegalStateException()

            return deserialize(json)
        }
    }

    return object : SimpleModule() {
        init {
            addSerializer(serializer)
            addDeserializer(T::class.java, deserializer)
        }
    }
}

fun createInstantModule(): SimpleModule {
    return createSimpleModule<Instant>(
        serialize = { it.toString() },
        deserialize = { OffsetDateTime.parse(it).toInstant() },
    )
}
