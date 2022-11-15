package persistence.postgres.queries.user

import models.DetailedUserReadPayload
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.USER
import persistence.postgres.mappings.detailedUserReadToJson

fun KotlinTransactionContext.getDetailedUser(userId: String): DetailedUserReadPayload? {
    return db.select(detailedUserReadToJson(user = USER))
        .from(USER)
        .where(USER.ID.eq(userId))
        .fetchOneInto(DetailedUserReadPayload::class.java)
}
