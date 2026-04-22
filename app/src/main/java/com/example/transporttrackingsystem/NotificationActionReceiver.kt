package com.example.transporttrackingsystem

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_MUTE") {
            // 🔇 Turn off the alerts in background
            MainActivity.isAlertsEnabled = false
        }
    }
}
