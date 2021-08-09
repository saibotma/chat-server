package persistence.jooq

import org.jooq.Converter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InstantConverter : Converter<OffsetDateTime, Instant> {
    override fun from(databaseObject: OffsetDateTime?): Instant? {
        return databaseObject?.toInstant()
    }

    override fun to(userObject: Instant?): OffsetDateTime? {
        return if (userObject != null) OffsetDateTime.ofInstant(userObject, ZoneOffset.UTC) else null
    }

    override fun fromType(): Class<OffsetDateTime> {
        return OffsetDateTime::class.java
    }

    override fun toType(): Class<Instant> {
        return Instant::class.java
    }
}
