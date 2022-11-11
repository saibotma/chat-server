package testutil.servertest.post

import io.ktor.client.statement.*
import io.ktor.server.testing.*
import models.ChannelMemberReadPayload
import models.ChannelMemberWritePayload
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureSuccess
import java.util.*

suspend fun ServerTestEnvironment.addMember(
    channelId: UUID,
    member: ChannelMemberWritePayload,
    handleResponse: suspend HttpResponse.(ChannelMemberWritePayload, ChannelMemberReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
): Pair<ChannelMemberWritePayload, ChannelMemberReadPayload?> {
    return post(member, "/platform/channels/$channelId/members", handleResponse = handleResponse)
}
