import clientapi.ClientApiConfig
import clientapi.ClientApiJwtAuthenticationPrinciple
import clientapi.installClientApi
import clientapi.installClientApiJwtAuthentication
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import error.ApiException
import persistence.postgres.ChatServerPostgres
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kodein.setupKodein
import logging.Logging
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.DIFeature
import org.kodein.di.ktor.closestDI
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.getUser
import platformapi.PlatformApiConfig
import platformapi.installPlatformApi
import platformapi.installPlatformApiAccessTokenAuthentication
import util.GenericTypeConversionService
import java.time.Instant
import java.time.LocalDate
import java.util.*
import kotlin.reflect.typeOf


fun Application.module() {
    installFeatures()

    val postgres: ChatServerPostgres by closestDI().instance()
    postgres.runMigration()

    routing {
        route("/platform") {
            installPlatformApi()
        }
        route("/client") {
            installClientApi()
        }
    }
}

private fun Application.installFeatures() {
    install(DIFeature) { setupKodein() }
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
                is ApiException -> {
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
