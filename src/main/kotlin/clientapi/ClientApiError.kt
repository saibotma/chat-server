package clientapi

data class ClientApiError(val errorCode: Int, val message: String? = null)
