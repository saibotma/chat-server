package graphql

import clientapi.AuthContext
import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import io.ktor.auth.*
import io.ktor.request.*

class KtorGraphQLContextFactory : GraphQLContextFactory<AuthContext, ApplicationRequest> {
    override suspend fun generateContext(request: ApplicationRequest): AuthContext? = request.call.principal()
}
