package push

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import org.apache.logging.log4j.kotlin.logger
import persistence.jooq.tables.pojos.FirebasePushToken
import util.convertToStringMap


class PushNotificationSender(private val objectMapper: ObjectMapper) {
    /**
     * Returns false when the token is not registered. True otherwise.
     */
    fun send(firebasePushToken: FirebasePushToken, pushNotification: PushNotification): Boolean {
        val message = newFirebaseNotification {
            iOS {
                setTitle(pushNotification.title)
                setBody(pushNotification.message)
            }
            android {
                setTitle(pushNotification.title)
                setBody(pushNotification.message)
            }

            putAllData(objectMapper.convertToStringMap(pushNotification.data))
            setToken(firebasePushToken.pushToken)
        }

        try {
            FirebaseMessaging.getInstance().send(message)
            return true
        } catch (t: FirebaseMessagingException) {
            if (t.errorCode == "UNREGISTERED") {
                logger().info("Firebase token $firebasePushToken not registered.", t)
                return false
            }
            logger().warn("Error when sending push notification to token $firebasePushToken.", t)
            return true
        }
    }
}
