package apis.clientapi

import apis.authentication.clientApiJwtAuthentication
import apis.clientapi.channel.createChannel
import apis.clientapi.channel.deleteChannel
import apis.clientapi.channel.updateChannel
import apis.clientapi.channelcontent.createChannelContent
import config.WebhookConfig
import io.ktor.client.*
import io.ktor.server.auth.*
import io.ktor.server.locations.*
import io.ktor.server.locations.post
import io.ktor.server.locations.put
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import persistence.jooq.KotlinDslContext
import java.util.*

@Location("channels")
object Channels {
    @Location("{channelId}")
    data class Channel(val channelId: UUID) {
        @Location("content")
        data class ChannelContent(val channel: Channel)
    }
}

fun Routing.installClientApi() {
    // TODO(saibotma): Add cors and csrf protection etc.

    val di = closestDI()
    val kotlinDslContext: KotlinDslContext by di.instance()
    val httpClient: HttpClient by di.instance()
    val webhookConfig: WebhookConfig by di.instance()

    authenticate(clientApiJwtAuthentication) {
        post<Channels> {
            createChannel(
                location = it,
                postgres = kotlinDslContext,
                client = httpClient,
                config = webhookConfig
            )
        }
        put<Channels.Channel> { updateChannel(location = it, postgres = kotlinDslContext) }
        delete<Channels.Channel> { deleteChannel(location = it, postgres = kotlinDslContext) }

        post<Channels.Channel.ChannelContent> { createChannelContent(location = it, postgres = kotlinDslContext) }
    }
}
