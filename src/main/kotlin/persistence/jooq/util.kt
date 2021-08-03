package persistence.jooq

import org.jooq.Record
import org.jooq.impl.TableImpl

fun <T : TableImpl<I>, I : Record> T.funAlias(funName: String): T = `as`("${name}_$funName") as T
