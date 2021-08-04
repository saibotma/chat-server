package persistence.jooq

import org.jooq.Condition
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.impl.TableImpl

fun <T : TableImpl<I>, I : Record> T.funAlias(funName: String): T = `as`("${name}_$funName") as T

fun <T : Record> SelectConditionStep<T>.andIf(
    executeCondition: Boolean,
    condition: () -> Condition
): SelectConditionStep<T> = if (executeCondition) and(condition()) else this

fun <T : Record> SelectConditionStep<T>.orIf(
    executeCondition: Boolean,
    condition: () -> Condition
): SelectConditionStep<T> = if (executeCondition) or(condition()) else this

