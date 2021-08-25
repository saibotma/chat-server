package graphql

import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.types.GraphQLServerRequest
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.request.*

class KtorGraphQLRequestParser : GraphQLRequestParser<ApplicationRequest> {

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun parseRequest(request: ApplicationRequest): GraphQLServerRequest = request.call.receive()
}
