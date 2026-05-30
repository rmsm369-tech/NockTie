package com.nyxtesla.talk2u

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService : FirebaseMessagingService() {
    private val TAG = "FcmService"

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            val title = message.notification?.title ?: "Notification"
            val body = message.notification?.body ?: message.data["body"] ?: "You have a new message."
            NotificationHelper.notifyImmediate(applicationContext, 2000, title, body)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to show notification", e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // TODO: send token to backend or Firestore 'users' document if needed
    }
}
