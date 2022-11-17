package clientapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@JvmInline
value class UserId(val value: String)


/**
 * A [Session] is just a wrapper 🎁 around a [Channel], with the ability to be closed.
 * It is meant to be used in a [SessionManager].
 */
interface Session<E, R> {
    val outgoing: SendChannel<E>
    val incoming: ReceiveChannel<E>
    suspend fun close(reason: R)
}

/**
 * A simplification of [DefaultWebSocketSession].
 * Wraps [DefaultWebSocketSession.outgoing], [DefaultWebSocketSession.incoming] and
 * [DefaultWebSocketSession.close].
 */
class SimpleWebSocketSession(private val defaultWebSocketSession: DefaultWebSocketSession) :
    Session<Frame, CloseReason> {
    override val outgoing: SendChannel<Frame>
        get() = defaultWebSocketSession.outgoing
    override val incoming: ReceiveChannel<Frame>
        get() = defaultWebSocketSession.incoming

    override suspend fun close(reason: CloseReason) = defaultWebSocketSession.close(reason)
}


/**
 * Assigns [Session]s to specific [UserId]s.
 */
interface SessionManager<E, R> {
    suspend fun maybeAddSession(userId: UserId, session: Session<E, R>)
    suspend fun removeSession(session: Session<E, R>)
    suspend fun hasOpenSessions(userId: UserId): Boolean
    suspend fun closeAllSessions(closeReason: CloseReason)
    suspend fun lock()
    suspend fun unlock()
}

/**
 * A [SessionManager] implementation for the Ktor web socket library.
 */
class TargetedMessageSessionManager(private val objectMapper: ObjectMapper) : TargetedMessageDispatcher,
    SessionManager<Frame, CloseReason> {
    // From: https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html#mutual-exclusion
    private val mutex = Mutex()
    private val sessions: MutableMap<UserId, MutableSet<Session<Frame, CloseReason>>> = mutableMapOf()
    private val isLocked: AtomicBoolean = AtomicBoolean(false)

    override suspend fun maybeAddSession(userId: UserId, session: Session<Frame, CloseReason>) {
        if (isLocked.get()) {
            session.close(CloseReason(CloseReason.Codes.TRY_AGAIN_LATER, "Service temporarily halted"))
        }
        mutex.withLock {
            val userSessions = sessions[userId] ?: mutableSetOf()
            userSessions.add(session)
            sessions[userId] = userSessions
        }
    }

    override suspend fun removeSession(session: Session<Frame, CloseReason>) = mutex.withLock {
        val keysToRemove = mutableSetOf<UserId>()
        for (entry in sessions) {
            if (entry.value.isEmpty()) {
                keysToRemove.add(entry.key)
                continue
            }
            entry.value.remove(session)
        }
        for (key in keysToRemove) {
            sessions.remove(key)
        }
    }

    override suspend fun hasOpenSessions(userId: UserId) = mutex.withLock { sessions[userId] != null }

    override suspend fun closeAllSessions(closeReason: CloseReason) = mutex.withLock {
        for (session in sessions.values.flatten()) {
            session.close(closeReason)
        }
    }

    override suspend fun lock() = isLocked.set(true)

    override suspend fun unlock() = isLocked.set(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun <T : Any> dispatch(userIds: Set<UserId>, message: T) {
        val json = objectMapper.writeValueAsString(message)
        mutex.withLock {
            for (userId in userIds) {
                val userSessions = sessions[userId] ?: continue
                for (session in userSessions) {
                    if (!session.outgoing.isClosedForSend) {
                        session.outgoing.send(Frame.Text(json))
                    }
                }
            }
        }
    }
}
