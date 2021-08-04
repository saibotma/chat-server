package clientapi

import graphql.ExecutionInput
import graphql.GraphQL
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

private data class GraphQLRequest(
    val query: String? = null,
    val operationName: String? = null,
    val variables: Map<String, Any>? = null
)

fun Route.installClientApi() {
    val graphQL: GraphQL by closestDI().instance()

    // To get the GraphQL schema comment this back in and
    // remove the "appWriteAuthenticate" block
    // installGraphQlPlayground()

    clientApiJwtAuthenticate {
        post("/graphql") {
            val authContext = call.principal<AuthContext>()
            val request = call.receive<GraphQLRequest>()
            val executionInput = ExecutionInput.newExecutionInput()
                .context(authContext)
                .query(request.query)
                .operationName(request.operationName)
                .variables(request.variables ?: emptyMap())
                .build()

            call.respond(graphQL.execute(executionInput))
        }
    }
}

private fun Routing.installGraphQlPlayground() {
    static("/graphql") {
        resource("/", "graphql-playground.html")
    }
}
