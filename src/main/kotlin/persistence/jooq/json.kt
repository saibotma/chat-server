package app.appella.persistence.jooq

import org.jooq.Condition
import org.jooq.Field
import org.jooq.JSON
import org.jooq.JSONObjectNullStep
import org.jooq.impl.DSL.*

fun jsonArrayAggNoNull(value: Field<*>): Field<JSON> {
    return coalesce(jsonArrayAgg(value).absentOnNull(), inline(JSON.json("[]")))
}

fun jsonObjectNullable(condition: Condition, jsonObject: JSONObjectNullStep<JSON>): Field<JSON> {
    return `when`(condition, castNull(JSON::class.java)).otherwise(jsonObject)
}
