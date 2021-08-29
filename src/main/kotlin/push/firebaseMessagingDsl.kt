package push

import com.google.firebase.messaging.*

fun newFirebaseNotification(init: Message.Builder.() -> Unit): Message {
    return Message.builder().also(init).build()
}

fun Message.Builder.android(init: AndroidNotification.Builder.() -> Unit) {
    setAndroidConfig(
        AndroidConfig
            .builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .setNotification(AndroidNotification.builder().also(init).build())
            .build()
    )
}

fun Message.Builder.iOS(init: ApsAlert.Builder.() -> Unit) {
    setApnsConfig(
        ApnsConfig
            .builder()
            .setAps(Aps.builder().setCategory("message").setAlert(ApsAlert.builder().also(init).build()).build())
            .build()
    )
}
