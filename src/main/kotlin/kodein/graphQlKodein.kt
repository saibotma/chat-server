package app.storyways.kodein

import app.storyways.graphql.DefaultDataFetcherExceptionHandler
import app.storyways.graphql.StorywaysSchemaGeneratorHooks
import app.storyways.graphql.mutations.TestMutation
import app.storyways.graphql.newGraphQL
import app.storyways.graphql.queries.TestQuery
import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.toSchema
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.GraphQL
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

val graphQLKodein = Kodein.Module("graphQl") {
    bind<TestQuery>() with singleton { TestQuery() }

    bind<TestMutation>() with singleton { TestMutation() }

    bind<DefaultDataFetcherExceptionHandler>() with singleton { DefaultDataFetcherExceptionHandler(instance()) }
    bind<GraphQL>() with singleton {
        val objectMapper = instance<ObjectMapper>()
        val config = SchemaGeneratorConfig(
            supportedPackages = listOf(""), //TODO add supported packages
            hooks = StorywaysSchemaGeneratorHooks(),
            dataFetcherFactoryProvider = SimpleKotlinDataFetcherFactoryProvider(objectMapper)
        )

        val queries = listOf(instance<TestQuery>()).map { TopLevelObject(it) }
        val mutations = listOf(instance<TestMutation>()).map { TopLevelObject(it) }

        newGraphQL(
            schema = toSchema(config, queries, mutations),
            dataFetcherExceptionHandler = instance()
        )
    }
}
