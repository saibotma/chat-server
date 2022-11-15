package persistence.postgres.queries.user

import models.DetailedUserReadPayload
import org.jooq.impl.DSL.value
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.tables.references.USER
import persistence.postgres.mappings.detailedUserReadToJson
import persistence.postgres.queries.channelmember.areMembersOfSameChannel

fun KotlinTransactionContext.getContactsOf(userId: String): List<DetailedUserReadPayload> {
    return db.select(detailedUserReadToJson(user = USER))
        .from(USER)
        .where(areMembersOfSameChannel(userId1 = USER.ID, userId2 = value(userId)))
        .fetchInto(DetailedUserReadPayload::class.java)
}
