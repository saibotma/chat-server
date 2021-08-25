package clientapi

import com.expediagroup.graphql.server.execution.GraphQLServer
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.KtorGraphQLServer
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.installClientApi() {
    val graphQLServer: GraphQLServer<ApplicationRequest> by closestDI().instance()
    val objectMapper: ObjectMapper by closestDI().instance()

    // To get the GraphQL schema comment this back in and
    // remove the authentication block.
    // installGraphQlPlayGround()

    clientApiJwtAuthenticate {
        post("/graphql") {
            // Execute the query against the schema
            val result = graphQLServer.execute(call.request)

            if (result != null) {
                // write response as json
                val json = objectMapper.writeValueAsString(result)
                call.respond(json)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid request")
            }
        }
    }
}

private fun Route.installGraphQlPlayground() {
    get("playground") {
        this.call.respondText(buildPlaygroundHtml("/client/graphql", "subscriptions"), ContentType.Text.Html)
    }
}

private fun buildPlaygroundHtml(graphQLEndpoint: String, subscriptionsEndpoint: String) =
    Application::class.java.classLoader.getResource("graphql-playground.html")?.readText()
        ?.replace("\${graphQLEndpoint}", graphQLEndpoint)
        ?.replace("\${subscriptionsEndpoint}", subscriptionsEndpoint)
        ?: throw IllegalStateException("graphql-playground.html cannot be found in the classpath")
