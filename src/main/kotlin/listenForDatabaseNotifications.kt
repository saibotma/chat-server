import clientapi.TargetedMessageSessionManager
import clientapi.models.toReadPayload
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.*
import io.ktor.websocket.*
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import org.apache.logging.log4j.kotlin.logger
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import persistence.jooq.KotlinDslContext
import persistence.postgres.PostgresConfig
import persistence.postgres.queries.channelevent.getChannelEvent
import persistence.postgres.queries.channelevent.getUserIdsForChannelEvent

fun Application.listenForDatabaseNotifications() {
    val log = logger(Application::listenForDatabaseNotifications.name)
    val sessionManager: TargetedMessageSessionManager by closestDI().instance()
    val objectMapper: ObjectMapper by closestDI().instance()

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
        suspend fun connectAndListen() {
            suspend fun handleDisconnect() {
                sessionManager.lock()
                sessionManager.closeAllSessions(
                    closeReason = CloseReason(
                        CloseReason.Codes.INTERNAL_ERROR,
                        message = "Lost connection to database."
                    )
                )
                delay(1000)
                connectAndListen()
            }

            try {
                val connection = connectionFactory.create().awaitFirst()
                connection.createStatement("LISTEN user_event")
                    .execute().asFlow().collect()
                connection.createStatement("LISTEN channel_event")
                    .execute().asFlow().collect()

                sessionManager.unlock()
                connection.notifications.asFlow().collect { notification ->
                    val eventId = notification.parameter!!.toLong()
                    if (notification.name == "channel_event") {
                        val event = database.transaction { getChannelEvent(eventId) }
                            ?.toReadPayload(objectMapper = objectMapper)
                        if (event != null) {
                            val userIds = database.transaction { getUserIdsForChannelEvent(event.channelId) }
                            sessionManager.dispatch(userIds = userIds, message = event)
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("Lost notification database connection.", e)
                handleDisconnect()
            } finally {
                handleDisconnect()
            }
        }

        connectAndListen()
    }
}
