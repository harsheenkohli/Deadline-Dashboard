package com.example.deadlinedashboard.ui.weekly

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deadlinedashboard.data.model.Assignment
import com.example.deadlinedashboard.ui.utils.getDeadlineText
import com.example.deadlinedashboard.ui.utils.getUrgencyColor
import com.example.deadlinedashboard.viewmodel.AssignmentViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyOverviewScreen(viewModel: AssignmentViewModel, modifier: Modifier = Modifier) {
    val assignments by viewModel.allAssignments.collectAsState()
    val TAG = "WeeklyOverviewScreen"

    val calendar = Calendar.getInstance()
    val startOfWeek = calendar.clone() as Calendar
    startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
    val endOfWeek = startOfWeek.clone() as Calendar
    endOfWeek.add(Calendar.DAY_OF_WEEK, 6)

    val weeklyAssignments = assignments.filter {
        val deadlineCalendar = Calendar.getInstance()
        deadlineCalendar.timeInMillis = it.deadline
        deadlineCalendar.after(startOfWeek) && deadlineCalendar.before(endOfWeek)
    }
    Log.d(TAG, "Weekly assignments: $weeklyAssignments")

    val overdueAssignments = assignments.filter { it.deadline < System.currentTimeMillis() && it.progress < 100 }
    val subjectLoad = weeklyAssignments.groupingBy { it.subject }.eachCount()
    val completedCount = weeklyAssignments.count { it.progress == 100 }
    val totalCount = weeklyAssignments.size
    val consistencyStreak = getConsistencyStreak(assignments)

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        if (overdueAssignments.isNotEmpty()) {
            item {
                OverdueWarningBanner(overdueAssignments.size)
            }
        }

        item {
            Text("Consistency Streak", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        item {
            ConsistencyStreakIndicator(streak = consistencyStreak)
        }

        item {
            Text("Weekly Priority Heatmap", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        item {
            WeeklyPriorityHeatmap(weeklyAssignments)
        }

        item {
            Text("Weekly Timeline", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        item {
            WeeklyTimeline(weeklyAssignments)
        }

        item {
            Text("Weekly Suggestion", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        item {
            WeeklyStudySuggestionCard(weeklyAssignments)
        }

        item {
            Text("Weekly Progress", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        item {
            WeeklyCompletionProgressBar(completedCount, totalCount)
        }

        item {
            Text("Subject-wise Load", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        items(subjectLoad.toList()) { (subject, count) ->
            SubjectLoadCard(subject, count)
        }

        item {
            Text("Upcoming Deadlines", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        items(weeklyAssignments) { assignment ->
            AssignmentCard(assignment)
        }
    }
}

@Composable
fun OverdueWarningBanner(overdueCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Red),
    ) {
        Text(
            text = "$overdueCount overdue tasks from last week - review now.",
            modifier = Modifier.padding(16.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConsistencyStreakIndicator(streak: Int) {
    if (streak > 1) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "You've completed at least one task for $streak days in a row! Keep it up!",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getConsistencyStreak(assignments: List<Assignment>): Int {
    if (assignments.isEmpty()) return 0

    val completedDates = assignments
        .filter { it.progress == 100 && it.completionDate != null }
        .map { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.completionDate!!
            cal.get(Calendar.DAY_OF_YEAR)
        }
        .toSet()

    var streak = 0
    val today = Calendar.getInstance()
    while (completedDates.contains(today.get(Calendar.DAY_OF_YEAR))) {
        streak++
        today.add(Calendar.DAY_OF_YEAR, -1)
    }
    return streak
}

@Composable
fun WeeklyPriorityHeatmap(assignments: List<Assignment>) {
    val deadlinesByDay = assignments.groupBy {
        val cal = Calendar.getInstance()
        cal.timeInMillis = it.deadline
        cal.get(Calendar.DAY_OF_YEAR)
    }

    val calendar = Calendar.getInstance()
    val startOfWeek = calendar.clone() as Calendar
    startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)

    val days = (0..6).map { day ->
        val date = startOfWeek.clone() as Calendar
        date.add(Calendar.DAY_OF_WEEK, day)
        date
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            val workload = deadlinesByDay[day.get(Calendar.DAY_OF_YEAR)]?.size ?: 0
            val color = when {
                workload >= 3 -> Color.Red
                workload > 0 -> Color.Yellow
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = SimpleDateFormat("EEE", Locale.getDefault()).format(day.time))
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color, shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun WeeklyTimeline(assignments: List<Assignment>) {
    val calendar = Calendar.getInstance()
    val startOfWeek = calendar.clone() as Calendar
    startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)

    val days = (0..6).map { day ->
        val date = startOfWeek.clone() as Calendar
        date.add(Calendar.DAY_OF_WEEK, day)
        date
    }

    val deadlinesByDay = assignments.groupBy {
        val cal = Calendar.getInstance()
        cal.timeInMillis = it.deadline
        cal.get(Calendar.DAY_OF_YEAR)
    }

    LazyRow(modifier = Modifier.fillMaxWidth()) {
        items(days) { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(text = SimpleDateFormat("EEE", Locale.getDefault()).format(day.time))
                Text(text = SimpleDateFormat("d", Locale.getDefault()).format(day.time))
                Spacer(modifier = Modifier.height(4.dp))
                if (deadlinesByDay.containsKey(day.get(Calendar.DAY_OF_YEAR))) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyStudySuggestionCard(assignments: List<Assignment>) {
    val suggestion = getWeeklyStudySuggestion(assignments)
    if (suggestion.isNotBlank()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = suggestion,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getWeeklyStudySuggestion(assignments: List<Assignment>): String {
    if (assignments.isEmpty()) return ""

    val deadlinesByDay = assignments.groupBy {
        val cal = Calendar.getInstance()
        cal.timeInMillis = it.deadline
        cal.get(Calendar.DAY_OF_WEEK)
    }

    val busiestDay = deadlinesByDay.maxByOrNull { it.value.size }
    if (busiestDay != null && busiestDay.value.size > 1) {
        val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(busiestDay.value.first().deadline)
        return "$dayName is your busiest day. Plan ahead!"
    }

    val endOfWeekThreshold = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 4) // Thursday
    }

    val lateWeekDeadlines = assignments.count { it.deadline > endOfWeekThreshold.timeInMillis }
    if (lateWeekDeadlines > assignments.size / 2) {
        return "You have a busy end of the week. Try to get a head start!"
    }

    return "Looks like a balanced week. Keep up the good work!"
}

@Composable
fun WeeklyCompletionProgressBar(completed: Int, total: Int) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text("Completed: $completed / $total")
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SubjectLoadCard(subject: String, count: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = subject, fontWeight = FontWeight.Bold)
            Text(text = "$count tasks")
        }
    }
}

@Composable
fun AssignmentCard(assignment: Assignment) {
    val isComplete = assignment.progress == 100
    val cardColor = if (isComplete) Color(0xFF1E90FF) else getUrgencyColor(assignment.deadline)
    val textColor = if (isComplete) Color.White else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = assignment.title, fontWeight = FontWeight.Bold, color = textColor)
                Text(text = getDeadlineText(assignment.deadline), color = textColor)
            }
            Text(text = assignment.subject, color = textColor)
        }
    }
}
