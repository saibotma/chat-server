package graphql

import clientapi.ClientApiException
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import graphql.language.SourceLocation

class DefaultDataFetcherExceptionHandler(private val objectMapper: ObjectMapper) : DataFetcherExceptionHandler {
    override fun onException(parameters: DataFetcherExceptionHandlerParameters?): DataFetcherExceptionHandlerResult {
        return DataFetcherExceptionHandlerResult.Builder()
            .apply {
                val exception = parameters!!.exception
                val apiException = exception as? ClientApiException

                if (apiException != null) {
                    error(apiException.toError(objectMapper))
                } else {
                    throw parameters.exception
                }
            }
            .build()
    }
}

private fun ClientApiException.toError(objectMapper: ObjectMapper) = object : GraphQLError {
    override fun getMessage() = objectMapper.writeValueAsString(error)

    override fun getLocations() = emptyList<SourceLocation>()

    override fun getExtensions(): MutableMap<String, Any> = mutableMapOf("classification" to "ClientApiError")

    override fun getErrorType(): ErrorClassification {
        return object : ErrorClassification {
            override fun toSpecification(error: GraphQLError?) = "ClientApiError"
        }
    }
}
