package graphql

import clientapi.AuthContext
import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import com.expediagroup.graphql.server.execution.GraphQLRequestHandler
import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.execution.GraphQLServer
import io.ktor.request.*

class KtorGraphQLServer(
    requestParser: GraphQLRequestParser<ApplicationRequest>,
    contextFactory: GraphQLContextFactory<AuthContext, ApplicationRequest>,
    requestHandler: GraphQLRequestHandler
) : GraphQLServer<ApplicationRequest>(requestParser, contextFactory, requestHandler)
