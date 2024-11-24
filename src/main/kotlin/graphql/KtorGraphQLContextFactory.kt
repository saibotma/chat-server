package graphql

import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import graphqlclientapi.AuthContext
import io.ktor.server.auth.*
import io.ktor.server.request.*

class KtorGraphQLContextFactory : GraphQLContextFactory<AuthContext, ApplicationRequest> {
    override suspend fun generateContext(request: ApplicationRequest): AuthContext? = request.call.principal()
}
