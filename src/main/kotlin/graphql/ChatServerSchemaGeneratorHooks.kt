package graphql

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ChatServerSchemaGeneratorHooks : SchemaGeneratorHooks {
    private val graphQlUUIDType = newScalar {
        name("UUID")
        description("A type representing a formatted java.util.UUID")
        coercing(parseValue = { UUID.fromString(it) }, serialize = { it.toString() })
    }

    private val graphQlUtcDateTime = newScalar {
        name("UtcDateTime")
        description("A type representing a ISO 8601 UTC timestamp")
        coercing(
            parseValue = ::parseInstant, serialize = ::serializeInstant
        )
    }

    override fun willGenerateGraphQLType(type: KType) = when (type.classifier as? KClass<*>) {
        UUID::class -> graphQlUUIDType
        Instant::class -> graphQlUtcDateTime
        else -> null
    }
}

fun parseInstant(value: String?): Instant? {
    return when (value) {
        null -> null
        else -> OffsetDateTime.parse(value).toInstant()
    }
}

fun serializeInstant(value: Instant?) = value.toString()
