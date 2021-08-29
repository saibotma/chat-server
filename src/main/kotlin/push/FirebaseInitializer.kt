package push

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class FirebaseInitializer(private val firebaseOptions: FirebaseOptions) {
    fun init() {
        FirebaseApp.initializeApp(firebaseOptions)
    }
}
