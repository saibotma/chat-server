package persistence.jooq.converters

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.JSONB
import org.jooq.impl.AbstractConverter

class JsonbConverter :
    AbstractConverter<JSONB, Map<String, Any?>>(JSONB::class.java, Map::class.java as Class<Map<String, Any?>>) {
    private val objectMapper = jacksonObjectMapper()

    override fun from(databaseObject: JSONB?): Map<String, Any?>? {
        return if (databaseObject == null) null else objectMapper.readValue(databaseObject.data())
    }

    override fun to(userObject: Map<String, Any?>?): JSONB? {
        return if (userObject == null) null else JSONB.jsonb(objectMapper.writeValueAsString(userObject))
    }
}
