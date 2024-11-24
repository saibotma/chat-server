package graphqlclientapi

enum class ClientApiErrorType { ResourceNotFound, ChannelNameBlank, ChannelDescriptionBlank, ChannelMustHaveOneAdmin }
data class ClientApiError(val type: ClientApiErrorType, val message: String? = null)
