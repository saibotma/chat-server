package clientapi

/**
 * Enables to send messages to a set of users.
 */
interface TargetedMessageDispatcher {
    suspend fun <T : Any> dispatch(userIds: Set<UserId>, message: T)
}
