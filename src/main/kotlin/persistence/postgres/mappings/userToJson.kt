package persistence.postgres.mappings

import dev.saibotma.persistence.postgres.jooq.tables.pojos.User
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.jsonObject
import persistence.jooq.value
import dev.saibotma.persistence.postgres.jooq.tables.User as UserTable

fun userToJson(user: UserTable): JSONObjectNullStep<JSON> {
    return jsonObject(
        User::id.value(user.ID),
        User::name.value(user.NAME)
    )
}
