package di

import clientapi.AuthContext
import clientapi.mutations.ChannelMutation
import clientapi.mutations.MessageMutation
import clientapi.queries.ChannelQuery
import clientapi.queries.MessageQuery
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.execution.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.*
import graphql.GraphQL.newGraphQL
import graphql.execution.DataFetcherExceptionHandler
import graphql.schema.GraphQLSchema
import io.ktor.request.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val graphQlDi = DI.Module("graphql") {
    bind<ChannelQuery>() with singleton { ChannelQuery(instance()) }
    bind<MessageQuery>() with singleton { MessageQuery(instance()) }
    bind<ChannelMutation>() with singleton { ChannelMutation(instance()) }
    bind<MessageMutation>() with singleton { MessageMutation(instance()) }
    bind<GraphQLSchema>() with singleton {
        val objectMapper = jacksonObjectMapper().apply {
            registerModule(JavaTimeModule())
        }
        val config = SchemaGeneratorConfig(
            // TODO(saibotma): Adjust schema models
            supportedPackages = listOf(
                "app.appella.rrule.LocalRRule",
                "app.appella.models",
                "app.appella.persistence.postgres.jooq.tables.pojos",
                "app.appella.graphql.models",
            ),
            hooks = ChatServerSchemaGeneratorHooks(),
            dataFetcherFactoryProvider = SimpleKotlinDataFetcherFactoryProvider(objectMapper = objectMapper)
        )

        val queries = listOf(
            instance<ChannelQuery>(),
            instance<MessageQuery>(),
        ).map { TopLevelObject(it) }

        val mutations = listOf(
            instance<ChannelMutation>(),
            instance<MessageMutation>(),
        ).map { TopLevelObject(it) }

        toSchema(config, queries, mutations)
    }
    bind<DataFetcherExceptionHandler>() with singleton { DefaultDataFetcherExceptionHandler(instance()) }
    bind<GraphQL>() with singleton { newGraphQL(instance()).defaultDataFetcherExceptionHandler(instance()).build() }
    bind<GraphQLRequestHandler>() with singleton { GraphQLRequestHandler(instance()) }
    bind<GraphQLRequestParser<ApplicationRequest>>() with singleton { KtorGraphQLRequestParser(instance()) }
    bind<GraphQLContextFactory<AuthContext, ApplicationRequest>>() with singleton { KtorGraphQLContextFactory() }
    bind<DataLoaderRegistryFactory>() with singleton { KtorDataLoaderRegistryFactory() }
    bind<GraphQLServer<ApplicationRequest>>() with singleton { KtorGraphQLServer(instance(), instance(), instance()) }
}
