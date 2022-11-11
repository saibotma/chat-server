package testutil.servertest.put

import io.ktor.client.statement.*
import models.ChannelMemberReadPayload
import models.ChannelMemberWritePayload
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureSuccess
import java.util.*

suspend fun ServerTestEnvironment.updateMember(
    channelId: UUID,
    member: ChannelMemberWritePayload,
    handleResponse: suspend HttpResponse.(ChannelMemberWritePayload, ChannelMemberReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
): Pair<ChannelMemberWritePayload, ChannelMemberReadPayload?> {
    return put(member, "/platform/channels/$channelId/members/${member.userId}", handleResponse = handleResponse)
}
