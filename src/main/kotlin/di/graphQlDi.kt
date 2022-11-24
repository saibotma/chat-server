package di

import clientapi.AuthContext
import clientapi.mutations.ChannelMutation
import clientapi.mutations.ContactMutation
import clientapi.mutations.MessageMutation
import clientapi.mutations.PushMutation
import clientapi.queries.ChannelEventQuery
import clientapi.queries.ChannelQuery
import clientapi.queries.MessageQuery
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import com.expediagroup.graphql.server.execution.GraphQLRequestHandler
import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.execution.GraphQLServer
import graphql.*
import graphql.GraphQL.newGraphQL
import graphql.execution.DataFetcherExceptionHandler
import graphql.schema.GraphQLSchema
import io.ktor.server.request.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val graphQlDi = DI.Module("graphql") {
    bind<ChannelQuery>() with singleton { ChannelQuery(database = instance(), objectMapper = instance()) }
    bind<ChannelEventQuery>() with singleton { ChannelEventQuery(database = instance(), objectMapper = instance()) }
    bind<MessageQuery>() with singleton { MessageQuery(instance()) }

    bind<ChannelMutation>() with singleton { ChannelMutation(instance()) }
    bind<MessageMutation>() with singleton { MessageMutation(instance(), instance()) }
    bind<PushMutation>() with singleton { PushMutation(instance()) }
    bind<ContactMutation>() with singleton { ContactMutation(instance()) }

    bind<GraphQLSchema>() with singleton {
        val config = SchemaGeneratorConfig(
            supportedPackages = listOf(
                // TODO(saibotma): Also remove this, but think about enums.
                "persistence.jooq.tables.pojos",
                "clientapi.models",
                // TODO(saibotma): Remove models, because should not share models to better be able to avoid breaking changes.
                "models",
            ),
            hooks = ChatServerSchemaGeneratorHooks(),
            dataFetcherFactoryProvider = SimpleKotlinDataFetcherFactoryProvider()
        )

        val queries = listOf(
            instance<ChannelQuery>(),
            instance<ChannelEventQuery>(),
            instance<MessageQuery>(),
        ).map { TopLevelObject(it) }

        val mutations = listOf(
            instance<ChannelMutation>(),
            instance<MessageMutation>(),
            instance<PushMutation>(),
            instance<ContactMutation>(),
        ).map { TopLevelObject(it) }

        toSchema(config = config, queries = queries, mutations = mutations)
    }
    // Got most of the stuff from https://github.com/ExpediaGroup/graphql-kotlin/tree/master/examples/server/ktor-server/src/main/kotlin/com/expediagroup/graphql/examples/server/ktor.
    bind<DataFetcherExceptionHandler>() with singleton { DefaultDataFetcherExceptionHandler(instance()) }
    bind<GraphQL>() with singleton { newGraphQL(instance()).defaultDataFetcherExceptionHandler(instance()).build() }
    bind<GraphQLRequestHandler>() with singleton { GraphQLRequestHandler(instance()) }
    bind<GraphQLRequestParser<ApplicationRequest>>() with singleton { KtorGraphQLRequestParser() }
    bind<GraphQLContextFactory<AuthContext, ApplicationRequest>>() with singleton { KtorGraphQLContextFactory() }
    bind<GraphQLServer<ApplicationRequest>>() with singleton { KtorGraphQLServer(instance(), instance(), instance()) }
}
