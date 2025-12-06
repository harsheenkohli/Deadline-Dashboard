package com.example.deadlinedashboard.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.deadlinedashboard.viewmodel.AssignmentViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SummaryScreen(viewModel: AssignmentViewModel, modifier: Modifier = Modifier) {
    val assignments by viewModel.allAssignments.collectAsState()

    val today = Calendar.getInstance()
    val startOfWeek = today.clone() as Calendar
    startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)

    val weeklyAssignments = assignments.filter {
        val deadlineCalendar = Calendar.getInstance()
        deadlineCalendar.timeInMillis = it.deadline
        deadlineCalendar.after(startOfWeek)
    }

    val completedCount = weeklyAssignments.count { it.progress == 100 }
    val pendingCount = weeklyAssignments.size - completedCount

    val wins = mutableListOf<String>()
    if (completedCount > 0) {
        wins.add("You completed $completedCount high-priority tasks - good work!")
    }
    if (weeklyAssignments.none { it.deadline < today.timeInMillis && it.progress < 100 }) {
        wins.add("No overdue tasks this week.")
    }

    val subjectDifficulty = weeklyAssignments.groupBy { it.subject }.mapValues {
        val subjectAssignments = it.value
        val completed = subjectAssignments.count { it.progress == 100 }
        if (subjectAssignments.isEmpty()) 0f else completed.toFloat() / subjectAssignments.size.toFloat()
    }

    val areasToImprove = mutableListOf<String>()
    subjectDifficulty.forEach { (subject, completionRate) ->
        if (completionRate < 0.5f) {
            areasToImprove.add("Focus more on $subject.")
        }
    }
    val overdueCount = assignments.count { it.deadline < today.timeInMillis && it.progress < 100 }
    if (overdueCount > 0) {
        areasToImprove.add("Complete your overdue tasks.")
    }

    val mostUrgentDay = weeklyAssignments
        .filter { it.progress < 100 }
        .groupBy { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.deadline
            cal.get(Calendar.DAY_OF_WEEK)
        }
        .maxByOrNull { it.value.size }?.key

    LazyColumn(modifier = modifier.padding(16.dp)) {
        item {
            Text("Weekly Summary", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            CompletedPendingChart(completedCount, pendingCount)
        }

        if (mostUrgentDay != null) {
            item {
                MostUrgentDayCard(day = SimpleDateFormat("EEEE", Locale.getDefault()).format(mostUrgentDay))
            }
        }

        item {
            Text("Subject Difficulty Heatmap", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
        }

        items(subjectDifficulty.toList()) { (subject, completionRate) ->
            SubjectDifficultyCard(subject, completionRate)
        }

        if (wins.isNotEmpty()) {
            item {
                Text("Wins of the Week:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            items(wins) { win ->
                Text(win)
            }
        }

        if (areasToImprove.isNotEmpty()) {
            item {
                Text("Areas to Improve:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            items(areasToImprove) { suggestion ->
                Text(suggestion)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.resetWeek() }, modifier = Modifier.fillMaxWidth()) {
                Text("Reset Week")
            }
        }
    }
}

@Composable
fun CompletedPendingChart(completed: Int, pending: Int) {
    val total = completed + pending
    if (total == 0) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(top = 16.dp)
    ) {
        if (completed > 0) {
            Box(
                modifier = Modifier
                    .weight(if (pending > 0) completed.toFloat() else 1f)
                    .fillMaxHeight()
                    .background(Color.Green)
            )
        }
        if (pending > 0) {
            Box(
                modifier = Modifier
                    .weight(if (completed > 0) pending.toFloat() else 1f)
                    .fillMaxHeight()
                    .background(Color.Red)
            )
        }
    }
}

@Composable
fun MostUrgentDayCard(day: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Text(text = "$day had the highest urgency load.", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SubjectDifficultyCard(subject: String, completionRate: Float) {
    val color = when {
        completionRate > 0.7f -> Color.Green
        completionRate > 0.4f -> Color.Yellow
        else -> Color.Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = subject, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color))
        }
    }
}
