package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notification.BpNotificationHelper

class BpNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("reminder_label") ?: "Blood Pressure Check"
        val reminderId = intent.getStringExtra("reminder_id") ?: ""

        BpNotificationHelper.showNotification(
            context = context,
            title = "Time for your $label",
            message = "Remember to sit relaxed with feet flat for a few minutes before taking your measurement.",
            notificationId = reminderId.hashCode()
        )
    }
}
