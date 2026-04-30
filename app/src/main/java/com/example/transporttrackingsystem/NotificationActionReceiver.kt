package com.example.transporttrackingsystem

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_MUTE") {
            // 🔇 Turn off the alerts in background
            MainActivity.isAlertsEnabled = false
        } else if (intent.action == "ACTION_ALARM") {
            // ⏰ OFFLINE ALARM TRIGGERED!
            val busId = intent.getStringExtra("BUS_ID") ?: "Your bus"
            val builder = androidx.core.app.NotificationCompat.Builder(context, "ARRIVE_CHANNEL")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Bus Arriving Soon! (Offline Alert)")
                .setContentText("$busId is arriving very soon. Head to the station!")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            if (androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33) {
                androidx.core.app.NotificationManagerCompat.from(context).notify(2, builder.build())
            }
        }
    }
}
