package testutil.servertest.post

import io.ktor.client.statement.*
import models.UserToken
import testutil.servertest.ServerTestEnvironment
import testutil.servertest.ensureSuccess

suspend fun ServerTestEnvironment.createUserToken(
    userId: String,
    handleResponse: suspend HttpResponse.(Unit, UserToken?) -> Unit = { _, _ -> ensureSuccess() }
): Pair<Unit, UserToken?> {
    return post(Unit, "/platform/users/$userId/tokens", handleResponse = handleResponse)
}
