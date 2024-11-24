package apis.clientapi.channelcontent

import apis.clientapi.Channels
import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext

suspend fun PipelineContext<Unit, ApplicationCall>.createChannelContent(
    location: Channels.Channel.ChannelContent,
    postgres: KotlinDslContext,
) {
 TODO()
}
