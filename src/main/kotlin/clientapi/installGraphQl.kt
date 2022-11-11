package clientapi

import clientapi.authentication.jwt.clientApiJwtAuthentication
import com.expediagroup.graphql.server.execution.GraphQLServer
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.installClientApi() {
    val graphQLServer: GraphQLServer<ApplicationRequest> by closestDI().instance()
    val objectMapper: ObjectMapper by closestDI().instance()

    // To get the GraphQL schema comment this back in and
    // remove the authentication block.
    // installGraphQlPlayground()

    authenticate(clientApiJwtAuthentication) {
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
