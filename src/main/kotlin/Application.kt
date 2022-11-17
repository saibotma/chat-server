
import clientapi.ClientApiConfig
import clientapi.TargetedMessageSessionManager
import clientapi.UserId
import clientapi.authentication.jwt.installClientApiJwtAuthentication
import clientapi.installClientApi
import clientapi.models.UserEventReadPayload
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.ConfigFactory
import di.setupDi
import error.PlatformApiException
import flyway.FlywayConfig
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.locations.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.dataconversion.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import logging.LoggingPlugin
import org.apache.logging.log4j.kotlin.logger
import org.flywaydb.core.Flyway
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import persistence.jooq.KotlinDslContext
import persistence.jooq.tables.references.USER
import persistence.jooq.tables.references.USER_EVENT
import persistence.postgres.PostgresConfig
import persistence.postgres.mappings.userEventReadPayloadToJson
import platformapi.PlatformApiConfig
import platformapi.authentication.accesstoken.installPlatformApiAccessTokenAuthentication
import platformapi.installPlatformApi
import push.FirebaseInitializer
import util.serverPort
import java.time.Instant
import java.time.LocalDate
import java.util.*

fun main() {
    // Execute using embedded server because wih automatic module loading
    // a second config file without module loading setting would be required
    // for tests. This would be a lot of duplicate config properties.
    embeddedServer(
        Netty,
        environment = applicationEngineEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load())
            connector {
                port = config.serverPort
                // Needs to be "0.0.0.0" otherwise it does not work
                // with docker and the current port forwarding configuration
                // in the docker compose files.
                host = "0.0.0.0"
            }
            module { chatServer() }
        }
    ).start(wait = true)
}

fun Application.chatServer(di: DI? = null) {
    installFeatures(di ?: DI { setupDi(environment.config) })

    val flywayConfig: FlywayConfig by closestDI().instance()
    val flyway: Flyway by closestDI().instance()
    // Need to support this in for databases that are not empty,
    // otherwise the migrations will not work.
    // In such a case the first (or the migrations until (including) the baseline)
    // have to be executed by hand.
    if (flywayConfig.shouldBaseline) flyway.baseline()
    flyway.migrate()

    val firebaseInitializer: Optional<FirebaseInitializer> by closestDI().instance()
    if (firebaseInitializer.isPresent) firebaseInitializer.get().init()

    routing {
        route("/platform") {
            installPlatformApi()
        }
        route("/client") {
            installClientApi()
        }
    }

    launchDatabaseListener()
}

fun Application.launchDatabaseListener() {
    val sessionManager: TargetedMessageSessionManager by closestDI().instance()
    val database: KotlinDslContext by closestDI().instance()
    val config: PostgresConfig by closestDI().instance()
    val connectionFactory = PostgresqlConnectionFactory(
        PostgresqlConnectionConfiguration.builder()
            .host(config.serverName)
            .port(config.port)
            .username(config.user)
            .password(config.password)
            .database(config.db)
            .build()
    )

    launch {
        // TODO(saibotma): Remove prints
        suspend fun connectAndListen() {
            suspend fun handleDisconnect() {
                sessionManager.lock()
                sessionManager.closeAllSessions(
                    closeReason = CloseReason(
                        CloseReason.Codes.INTERNAL_ERROR,
                        message = "Lost connection to database."
                    )
                )
                // TODO(saibotma): Cancel all websocket connections.
                println("Waiting to reconnect..")
                delay(1000)
                connectAndListen()
            }

            try {
                println("Connecting..")
                val connection = connectionFactory.create().awaitFirst()
                println("Connected")
                connection.createStatement("LISTEN user_event")
                    .execute().asFlow().collect()
                sessionManager.unlock()
                connection.notifications.asFlow().collect { notification ->
                    val event = database.transaction {
                        db.select(userEventReadPayloadToJson(userEvent = USER_EVENT))
                            .from(USER_EVENT)
                            .where(USER_EVENT.ID.eq(notification.parameter!!.toLong()))
                            .fetchOneInto(UserEventReadPayload::class.java)
                    }!!

                    val userIds = database.transaction {
                        db.select(USER.ID)
                            .from(USER)
                            //.where(areMembersOfSameChannel(userId1 = USER.ID, userId2 = value(event.userId)))
                            .fetchInto(String::class.java)
                    }.map(::UserId).toSet()
                    sessionManager.dispatch(userIds = userIds, message = event)
                }
            } catch (e: Exception) {
                println(e)
                handleDisconnect()
            } finally {
                handleDisconnect()
            }
        }

        connectAndListen()
    }
}

private fun Application.installFeatures(di: DI) {
    val log = logger()

    di { extend(di) }

    // Currently, only required for the Logging feature
    install(CallId) { generate(10, CALL_ID_DEFAULT_DICTIONARY) }
    install(DoubleReceive)
    install(Locations)
    install(LoggingPlugin)
    install(WebSockets)

    install(Authentication) {
        val platformApiConfig: PlatformApiConfig by di.instance()
        val clientApiConfig: ClientApiConfig by di.instance()
        val kotlinDslContext: KotlinDslContext by di.instance()

        installPlatformApiAccessTokenAuthentication(accessToken = platformApiConfig.accessToken)
        installClientApiJwtAuthentication(jwtSecret = clientApiConfig.jwtSecret, kotlinDslContext = kotlinDslContext)
    }

    install(ContentNegotiation) {
        jackson { di.direct.instance<ObjectMapper.() -> Unit>()() }
    }

    install(DataConversion) {
        convert<LocalDate> {
            decode { values -> LocalDate.parse(values.first()) }
        }

        convert<Instant> {
            decode { values -> Instant.parse(values.first()) }
        }

        convert<UUID> {
            decode { values -> UUID.fromString(values.first()) }
        }

        convert<List<UUID>> {
            decode { values -> values.first().split(",").map { UUID.fromString(it) } }
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is ContentTransformationException -> {
                    call.respondText(
                        "Request parameters could not be parsed. ${cause.message}", status = HttpStatusCode.BadRequest,
                    )
                }

                is ParameterConversionException -> {
                    call.respondText(
                        "Parameter \"${cause.parameterName}\" could not be parsed.\n" +
                                "Please check that the type fulfils the specification.",
                        status = HttpStatusCode.BadRequest,
                    )
                }

                is JsonMappingException -> {
                    val locationMessage = cause.location?.let {
                        "Error at line ${it.lineNr} and column ${it.columnNr}"
                    }
                    call.respondText(
                        "Json body could not be parsed. " +
                                "Please check that the values fulfil the specification ${locationMessage ?: ""}",
                        status = HttpStatusCode.BadRequest,
                    )
                }

                is com.fasterxml.jackson.core.JsonParseException -> {
                    call.respondText(
                        "Json body could not be parsed.\n${cause.originalMessage}",
                        status = HttpStatusCode.BadRequest
                    )
                }

                is PlatformApiException -> {
                    call.respond(cause.statusCode, cause.error)
                }

                else -> {
                    call.respond(HttpStatusCode.InternalServerError)
                    log.error("Unhandled exception", cause)
                }
            }
        }
    }
}
