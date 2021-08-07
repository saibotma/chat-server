package platformapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import persistence.jooq.KotlinDslContext
import persistence.postgres.queries.*
import models.ChannelWritePayload
import models.toChannel
import java.time.Instant
import java.util.UUID.randomUUID

suspend fun PipelineContext<Unit, ApplicationCall>.createChannel(
    location: ChannelList,
    database: KotlinDslContext
) {
    val channel = call.receive<ChannelWritePayload>()
    val result = database.transaction {
        val channelId = randomUUID()
        insertChannel(channel.toChannel(id = channelId, createdAt = Instant.now()))
        getChannel(channelId)
    }
    call.respond(HttpStatusCode.Created, result!!)
}
