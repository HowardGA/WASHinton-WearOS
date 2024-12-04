package com.example.washintonwearos.presentation.features.Notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.washintonwearos.R
import com.example.washintonwearos.presentation.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Send token to your server if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received from: ${remoteMessage.from}")
        Log.d("FCM", "Received data: ${remoteMessage.data}")

        // Parse the 'data' JSON string
        val dataJsonString = remoteMessage.data["data"] ?: "{}"
        val dataJson = JSONObject(dataJsonString)

        val transferId = dataJson.optInt("transfer_id", -1)
        val storeId = dataJson.optInt("store_id", -1)
        val transferDate = dataJson.optString("transfer_date", "Unknown Date")
        val status = dataJson.optString("status", "Unknown Status")
        val reasons = dataJson.optString("reasons", "No Reasons")
        val store = dataJson.optString("store", "Unknown Store")

        val detailsJsonArray = dataJson.optJSONArray("details")
        val details = detailsJsonArray?.let { parseDetails(it.toString()) } ?: emptyList()

        Log.d(
            "FCM",
            "Parsed data: Transfer ID = $transferId, Store ID = $storeId, Transfer Date = $transferDate, Status = $status, Reasons = $reasons, Store = $store, Details = $details"
        )
        Log.d("FCM", "Complete Data: ${remoteMessage.data}")

        // Optionally, show a notification
        val title = remoteMessage.data["title"] ?: "New Notification"
        val body = remoteMessage.data["body"] ?: "You have a new message."
        showNotification(applicationContext, title, body, transferId)

    }

    private fun showNotification(context: Context, title: String, message: String, orderID: Int) {
        val channelId = "default_channel_id"
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

        // Check and request notification permissions (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("FCM", "Notification permission not granted.")
            return
        }

        // Create notification channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General Notifications"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent and PendingIntent for the notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("transfer_id", orderID) // Pass the orderID to be picked up by NavHost
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and display the notification
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.notification_icon)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_cleanning)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Attach the PendingIntent
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
        Log.d("FCM", "Notification shown with ID: $notificationId")
    }
}

// Function to parse details JSON string into a list of details
fun parseDetails(detailsJson: String): List<Detail> {
    val gson = Gson()
    val type = object : TypeToken<List<Detail>>() {}.type
    return gson.fromJson(detailsJson, type)
}

// Data classes for parsing JSON
data class Detail(
    val transfer_detail_id: Int,
    val transfer_id: Int,
    val product_id: Int,
    val quantity: Int,
    val product: Product
)

data class Product(
    val product_id: Int,
    val name: String,
    val price: String
)
