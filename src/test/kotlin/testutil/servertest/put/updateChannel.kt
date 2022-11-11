package testutil.servertest.put

import io.ktor.client.statement.*
import models.ChannelReadPayload
import models.ChannelWritePayload
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureSuccess
import java.util.*

suspend fun ServerTestEnvironment.updateChannel(
    id: UUID,
    channel: ChannelWritePayload,
    handleResponse: suspend HttpResponse.(ChannelWritePayload, ChannelReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
): Pair<ChannelWritePayload, ChannelReadPayload?> {
    return put(channel, "/platform/channels/$id", handleResponse = handleResponse)
}
