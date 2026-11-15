package com.example.calendar1

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Locale

object TaskAlarmManager {

    fun schedule(context: Context, task: Task): Boolean {
        val format = SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        )

        format.isLenient = false

        val dateTime = try {
            format.parse("${task.date} ${task.time}")
        } catch (_: Exception) {
            null
        } ?: return false

        val triggerTime = dateTime.time

        // Не планируем напоминания на уже прошедшее время.
        if (triggerTime <= System.currentTimeMillis()) {
            return false
        }

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        val pendingIntent = createPendingIntent(
            context,
            task.id,
            task.title
        )

        return try {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                // Запасной вариант, если доступ к точным
                // будильникам пока не предоставлен.
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                false
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                true
            }
        } catch (_: SecurityException) {
            false
        }
    }

    fun cancel(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        val pendingIntent = createPendingIntent(
            context,
            taskId,
            null
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun createPendingIntent(
        context: Context,
        taskId: Int,
        taskTitle: String?
    ): PendingIntent {

        val intent = Intent(
            context,
            TaskReminderReceiver::class.java
        ).apply {
            action = "com.example.calendar1.TASK_REMINDER_$taskId"
            putExtra("task_id", taskId)

            if (taskTitle != null) {
                putExtra("task_title", taskTitle)
            }
        }

        return PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }
}
