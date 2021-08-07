package persistence.postgres.mappings

import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import persistence.jooq.value
import models.DetailedUserReadPayload
import dev.saibotma.persistence.postgres.jooq.tables.User as UserTable

fun detailedUserReadToJson(user: UserTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        DetailedUserReadPayload::id.value(user.ID),
        DetailedUserReadPayload::name.value(user.NAME),
        DetailedUserReadPayload::createdAt.value(user.CREATED_AT),
    )
}
