package persistence.jooq

import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import kotlin.reflect.KProperty

fun jsonArrayAggNoNull(value: Field<*>): Field<JSON> {
    return coalesce(jsonArrayAgg(value).absentOnNull(), inline(JSON.json("[]")))
}

fun jsonObjectNullable(condition: Condition, jsonObject: JSONObjectNullStep<JSON>): Field<JSON> {
    return `when`(condition, castNull(JSON::class.java)).otherwise(jsonObject)
}

fun <T, R> KProperty<T>.value(value: Field<R>) = key(this.name).value(value)
fun <T, R> KProperty<T>.value(value: R) = key(this.name).value(value)
fun <T, R> KProperty<T>.value(value: Select<out Record1<R>?>?) = key(this.name).value(value)
