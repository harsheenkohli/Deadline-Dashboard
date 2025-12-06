package com.example.deadlinedashboard.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.deadlinedashboard.data.model.Assignment
import com.example.deadlinedashboard.receiver.NotificationReceiver

object NotificationScheduler {

    fun scheduleNotification(context: Context, assignment: Assignment) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", assignment.id)
            putExtra("title", assignment.title)
            putExtra("message", "Your assignment is due soon!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignment.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val reminderTime = assignment.deadline - (assignment.reminderOffset ?: 0)

        if (reminderTime > System.currentTimeMillis()) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun cancelNotification(context: Context, assignment: Assignment) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignment.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}
