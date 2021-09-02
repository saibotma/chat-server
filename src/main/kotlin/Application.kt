import clientapi.ClientApiConfig
import clientapi.installClientApi
import clientapi.installClientApiJwtAuthentication
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import di.setupKodein
import error.PlatformApiException
import flyway.FlywayConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import logging.Logging
import org.flywaydb.core.Flyway
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.DIFeature
import org.kodein.di.ktor.closestDI
import persistence.jooq.KotlinDslContext
import platformapi.PlatformApiConfig
import platformapi.installPlatformApi
import platformapi.installPlatformApiAccessTokenAuthentication
import push.FirebaseInitializer
import util.GenericTypeConversionService
import java.time.Instant
import java.time.LocalDate
import java.util.*
import kotlin.reflect.typeOf


fun Application.module(bindDependencies: DI.MainBuilder.() -> Unit = { setupKodein() }) {
    installFeatures(bindDependencies)
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
}

private fun Application.installFeatures(bindDependencies: DI.MainBuilder.() -> Unit) {
    install(DIFeature) { bindDependencies() }
    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        header(HttpHeaders.Accept)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.ContentType)
        allowCredentials = true
        // TODO(saibotma): Make more strict
        anyHost()
    }
    // Currently only required for the Logging feature
    install(CallId) {
        // No special reason why this uses this generator
        generate(10)
    }
    install(DoubleReceive)
    install(Logging) {
        logRequests = true
        logResponses = true
        logHeaders = true
        logBody = false
        logFullUrl = true
    }
    install(Locations) {}
    install(Authentication) {
        val platformApiConfig: PlatformApiConfig by closestDI().instance()
        val clientApiConfig: ClientApiConfig by closestDI().instance()
        val kotlinDslContext: KotlinDslContext by closestDI().instance()

        installPlatformApiAccessTokenAuthentication(expectedAccessToken = platformApiConfig.accessToken)
        installClientApiJwtAuthentication(jwtSecret = clientApiConfig.jwtSecret, kotlinDslContext = kotlinDslContext)
    }
    install(ContentNegotiation) {
        jackson { closestDI().direct.instance<ObjectMapper.() -> Unit>()() }
    }

    install(DataConversion) {
        convert<LocalDate> {
            decode { values, _ -> LocalDate.parse(values.first()) }
        }

        convert<Instant> {
            decode { values, _ -> Instant.parse(values.first()) }
        }

        convert<UUID> {
            decode { values, _ -> UUID.fromString(values.first()) }
        }

        val type = typeOf<List<UUID>>()
        convert(type, GenericTypeConversionService(type::class).apply {
            decode { values, _ -> values.first().split(",").map { UUID.fromString(it) } }
        })
    }

    install(StatusPages) {
        exception<Throwable> { t ->
            when (t) {
                is ContentTransformationException -> {
                    call.respond(
                        HttpStatusCode.BadRequest, """
                        Request parameters could not be parsed.
                        ${t.message}
                    """.trimIndent()
                    )
                }
                is ParameterConversionException -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Parameter \"${t.parameterName}\" could not be parsed.\nPlease check that the type fulfils the specification."
                    )
                }
                is JsonMappingException -> {
                    val locationMessage = t.location?.let {
                        "Error at line ${it.lineNr} and column ${it.columnNr}"
                    }
                    call.respond(
                        HttpStatusCode.BadRequest,
                        """
                            Json body could not be parsed.
                            Please check that the values fulfil the specification.
                            ${locationMessage ?: ""}
                        """.trimIndent()
                    )
                }
                is com.fasterxml.jackson.core.JsonParseException -> {
                    call.respond(HttpStatusCode.BadRequest, "Json body could not be parsed.\n${t.originalMessage}")
                }
                is PlatformApiException -> {
                    call.respond(t.statusCode, t.error)
                }
                else -> {
                    call.respond(HttpStatusCode.InternalServerError)
                    throw t
                }
            }
        }
    }
}
