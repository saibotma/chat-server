package testutil.servertest.put

import io.ktor.client.statement.*
import models.ChannelMemberReadPayload
import models.ChannelMemberWritePayload
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureSuccess
import java.util.*

suspend fun ServerTestEnvironment.setMembers(
    channelId: UUID,
    members: List<ChannelMemberWritePayload>,
    handleResponse: suspend HttpResponse.(List<ChannelMemberWritePayload>, List<ChannelMemberReadPayload>?) -> Unit = { _, _ -> ensureSuccess() }
): Pair<List<ChannelMemberWritePayload>, List<ChannelMemberReadPayload>?> {
    return put(members, "/platform/channels/$channelId/members", handleResponse = handleResponse)
}
