package clientapi

import com.expediagroup.graphql.server.execution.GraphQLServer
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Route.installClientApi() {
    val graphQLServer: GraphQLServer<ApplicationRequest> by closestDI().instance()
    val objectMapper: ObjectMapper by closestDI().instance()
    val socketManager: TargetedMessageSessionManager by closestDI().instance()

    // To get the GraphQL schema comment this back in and
    // remove the authentication block.
    installGraphQlPlayground()

    //authenticate(clientApiJwtAuthentication) {
        // TODO(saibotma): Rename to graphql-api and also move to apis folder.
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

        webSocket("/events", protocol = "chat-server-authenticated-client") {
            val context = call.principal<AuthContext>()!!
            val userId = UserId(context.userId)
            val session = SimpleWebSocketSession(this)
            socketManager.maybeAddSession(userId = userId, session = session)

            try {
                for (frame in incoming) {
                    continue
                }
            } finally {
                // Does not enter catch and thus need to handle web socket
                // close in finally block:
                // https://youtrack.jetbrains.com/issue/KTOR-5191/How-to-get-cancel-reason-when-client-closes-web-socket-connection
                socketManager.removeSession(session)
            }
        }
    //}
}

private fun Route.installGraphQlPlayground() {
    get("/playground") {
        this.call.respondText(buildPlaygroundHtml("client/graphql", "client/subscriptions"), ContentType.Text.Html)
    }
}

private fun buildPlaygroundHtml(graphQLEndpoint: String, subscriptionsEndpoint: String) =
    Application::class.java.classLoader.getResource("graphql-playground.html")?.readText()
        ?.replace("\${graphQLEndpoint}", graphQLEndpoint)
        ?.replace("\${subscriptionsEndpoint}", subscriptionsEndpoint)
        ?: throw IllegalStateException("graphql-playground.html cannot be found in the classpath")
