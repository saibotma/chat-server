package testutil.servertest

import error.PlatformApiException
import error.duplicate
import error.resourceNotFound
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.statement.*
import io.ktor.http.*

fun HttpResponse.ensureSuccess() {
    status shouldBeIn listOf(HttpStatusCode.OK, HttpStatusCode.NoContent, HttpStatusCode.Created)
}

fun HttpResponse.ensureRedirect(targetUrl: String) {
    status shouldBe HttpStatusCode.Found
    headers["Location"] shouldBe targetUrl
}

suspend fun HttpResponse.ensureNoContent() {
    status shouldBe HttpStatusCode.NoContent
    bodyAsText() shouldBe ""
}

suspend fun HttpResponse.ensureBadRequestWithDuplicate(
    duplicatePropertyName: String,
    duplicatePropertyValue: String,
) {
    val expectedError = PlatformApiException.duplicate(duplicatePropertyName, duplicatePropertyValue).error
    asApiError() shouldBe expectedError

    status shouldBe HttpStatusCode.BadRequest
}

suspend fun HttpResponse.ensureHasContent() {
    status shouldBe HttpStatusCode.OK
    bodyAsText() shouldNotBe ""
}

suspend fun HttpResponse.ensureResourceNotFound() {
    asApiError() shouldBe PlatformApiException.resourceNotFound().error
    status shouldBe HttpStatusCode.NotFound
}
