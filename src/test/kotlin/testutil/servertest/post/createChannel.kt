package testutil.servertest.post

import io.ktor.client.statement.*
import models.ChannelReadPayload
import models.ChannelWritePayload
import testutil.mockedChannelWrite
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureSuccess

suspend fun ServerTestEnvironment.createChannel(
    channel: ChannelWritePayload = mockedChannelWrite(),
    handleResponse: suspend HttpResponse.(ChannelWritePayload, ChannelReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
): Pair<ChannelWritePayload, ChannelReadPayload?> {
    return post(channel, "/platform/channels", handleResponse = handleResponse)
}

