package com.example.deadlinedashboard.ui.utils

import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun getUrgencyColor(deadline: Long): Color {
    val daysLeft = TimeUnit.MILLISECONDS.toDays(deadline - System.currentTimeMillis())
    return when {
        daysLeft < 0 -> Color(0xFFB00020) // Overdue - Dark Red
        daysLeft < 2 -> Color(0xFFEF5350) // Urgent - Red
        daysLeft <= 5 -> Color(0xFFFFEE58) // Medium - Yellow
        else -> Color(0xFF66BB6A) // Low - Green
    }
}

fun getDeadlineText(deadline: Long): String {
    val deadlineCalendar = Calendar.getInstance().apply {
        timeInMillis = deadline
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val difference = deadlineCalendar.timeInMillis - today.timeInMillis
    val daysLeft = TimeUnit.MILLISECONDS.toDays(difference)

    return when {
        daysLeft < 0 -> "Overdue by ${abs(daysLeft)} days"
        daysLeft == 0L -> "Due today"
        daysLeft == 1L -> "Due tomorrow"
        else -> "Due in $daysLeft days"
    }
}
