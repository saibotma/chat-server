package testutil.servertest.put

import io.ktor.client.statement.*
import models.UserReadPayload
import models.UserWritePayload
import testutil.mockedUser
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureSuccess
import java.util.*

suspend fun ServerTestEnvironment.upsertUser(
    id: String = UUID.randomUUID().toString(),
    user: UserWritePayload = mockedUser(),
    handleResponse: suspend HttpResponse.(UserWritePayload, UserReadPayload?) -> Unit = { _, _ -> ensureSuccess() }
): Pair<UserWritePayload, UserReadPayload?> {
    return put(user, "/platform/users/$id", handleResponse = handleResponse)
}
