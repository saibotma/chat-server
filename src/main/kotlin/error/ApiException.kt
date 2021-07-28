package error

import io.ktor.http.*
import java.util.*

data class ApiError(val errorCode: Int, val message: String? = null)

data class ApiException(
    val statusCode: HttpStatusCode,
    val error: ApiError,
) : Exception() {
    override val message = error.toString()

    companion object
}

fun ApiException.Companion.dependencyNotFound(
    propertyName: String? = null,
    propertyValue: String? = null
): ApiException {
    return ApiException(
        HttpStatusCode.BadRequest,
        ApiError(
            1,
            if (propertyName == null) "Could not be found" else "$propertyName = $propertyValue could not be found."
        )
    )
}

fun ApiException.Companion.duplicate(duplicatePropertyName: String, duplicatePropertyValue: String): ApiException {
    return ApiException(
        HttpStatusCode.BadRequest,
        ApiError(2, "Duplicate $duplicatePropertyName = \"$duplicatePropertyValue\"")
    )
}
