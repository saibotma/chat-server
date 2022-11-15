package persistence.postgres.queries.channelmember

import org.jooq.Condition
import org.jooq.Field
import org.jooq.impl.DSL.*
import persistence.jooq.KotlinTransactionContext
import persistence.jooq.funAlias
import persistence.jooq.tables.references.CHANNEL

fun KotlinTransactionContext.areMembersOfSameChannel(
    userId1: String,
    userId2: String
): Boolean {
    return db.select(areMembersOfSameChannel(userId1 = value(userId1), userId2 = value(userId2)))
        .fetchOneInto(Boolean::class.java)!!
}

fun areMembersOfSameChannel(userId1: Field<String?>, userId2: Field<String?>): Condition {
    val funName = ::areMembersOfSameChannel.name
    val channel = CHANNEL.funAlias(funName)

    return exists(
        selectFrom(channel)
            .where(isMemberOfChannel(channelId = channel.ID, userId = userId1))
            .and(isMemberOfChannel(channelId = channel.ID, userId = userId2))
    )
}
