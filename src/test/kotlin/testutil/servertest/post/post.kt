package testutil.servertest.post

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import testutil.servertest.ServerTestEnvironment

suspend inline fun <reified T, reified R> ServerTestEnvironment.post(
    resource: T,
    path: String,
    urlProtocol: URLProtocol = URLProtocol.HTTP,
    handleResponse: suspend HttpResponse.(T, R?) -> Unit,
): Pair<T, R?> {
    val response = client.post(path) {
        url {
            protocol = urlProtocol
        }
        contentType(ContentType.Application.Json)
        addApiKeyAuthentication()
        setBody(resource)
    }

    val result =
        if (response.status.isSuccessWithContent()) response.body<R>() else null
    response.handleResponse(resource, result)
    return resource to result
}
