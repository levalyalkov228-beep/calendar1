package com.example.calendar1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("task_id", 0)
        val taskTitle = intent.getStringExtra("task_title")
            ?: "У вас запланирована задача"

        NotificationHelper.showNotification(
            context,
            taskId,
            taskTitle
        )
    }
}
