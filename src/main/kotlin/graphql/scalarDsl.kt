package graphql

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType

fun newScalar(init: GraphQLScalarType.Builder.() -> Unit): GraphQLScalarType {
    return GraphQLScalarType.newScalar().apply(init).build()
}

inline fun <reified T> GraphQLScalarType.Builder.coercing(
    noinline parseValue: (String?) -> T,
    noinline serialize: (T) -> String,
) {
    coercing(object : Coercing<T, String> {
        override fun parseValue(input: Any): T = parseValue(serialize(input))

        override fun parseLiteral(input: Any): T {
            val string = (input as? StringValue)?.value
            return parseValue(string)
        }

        override fun serialize(dataFetcherResult: Any): String =
            (dataFetcherResult as? T)?.let(serialize) ?: dataFetcherResult.toString()
    })
}
