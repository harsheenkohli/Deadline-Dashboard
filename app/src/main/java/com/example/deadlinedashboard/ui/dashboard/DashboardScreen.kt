package com.example.deadlinedashboard.ui.dashboard

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deadlinedashboard.data.model.Assignment
import com.example.deadlinedashboard.ui.utils.getDeadlineText
import com.example.deadlinedashboard.ui.utils.getUrgencyColor
import com.example.deadlinedashboard.utils.NotificationScheduler
import com.example.deadlinedashboard.viewmodel.AssignmentViewModel
import java.util.Calendar
import kotlin.math.abs

enum class SortOption {
    Urgency,
    Deadline,
    Subject,
    Weightage
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AssignmentViewModel,
    sortOption: SortOption,
    onEditClick: (Assignment) -> Unit,
    modifier: Modifier = Modifier
) {
    val assignments by viewModel.allAssignments.collectAsState()
    val context = LocalContext.current
    val TAG = "DashboardScreen"

    Log.d(TAG, "DashboardScreen recomposed with sort option: $sortOption")

    val sortedAssignments = when (sortOption) {
        SortOption.Urgency -> assignments.sortedWith(compareBy({ it.deadline < System.currentTimeMillis() }, { it.deadline }))
        SortOption.Deadline -> assignments.sortedBy { it.deadline }
        SortOption.Subject -> assignments.sortedBy { it.subject }
        SortOption.Weightage -> assignments.sortedByDescending { it.weightage }
    }

    if (sortedAssignments.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("You're all caught up!")
        }
    } else {
        LazyColumn(modifier = modifier) {
            item {
                DailySuggestionCard(assignments)
            }
            items(sortedAssignments, key = { it.id }) { assignment ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                Log.d(TAG, "Assignment marked as done: $assignment")
                                viewModel.update(assignment.copy(progress = 100, completionDate = System.currentTimeMillis()))
                                true
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                Log.d(TAG, "Assignment deleted: $assignment")
                                viewModel.delete(assignment)
                                NotificationScheduler.cancelNotification(context, assignment)
                                true
                            }
                            SwipeToDismissBoxValue.Settled -> false
                        }
                    },
                    positionalThreshold = { it * 0.5f }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val direction = dismissState.targetValue
                        val color by animateColorAsState(
                            targetValue = when (direction) {
                                SwipeToDismissBoxValue.EndToStart -> Color.Red
                                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF1E90FF)
                                SwipeToDismissBoxValue.Settled -> Color.Transparent
                            },
                            label = "background_color"
                        )
                        val alignment = when (direction) {
                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                            SwipeToDismissBoxValue.Settled -> Alignment.CenterStart
                        }
                        val icon = when (direction) {
                            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                            SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                            SwipeToDismissBoxValue.Settled -> null
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            if (icon != null) {
                                Icon(
                                    icon,
                                    contentDescription = "Action",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    content = {
                        AssignmentCard(
                            assignment = assignment,
                            onEditClick = onEditClick,
                            onMarkAsCompleteClick = {
                                viewModel.update(assignment.copy(progress = 100, completionDate = System.currentTimeMillis()))
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun DailySuggestionCard(assignments: List<Assignment>) {
    val TAG = "DailySuggestionCard"
    val suggestion = assignments.filter { it.progress < 100 && it.deadline > System.currentTimeMillis() }
        .minByOrNull { it.deadline }

    if (suggestion != null) {
        Log.d(TAG, "Suggestion: $suggestion")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Today, complete: ${suggestion.title} - ${100 - suggestion.progress}% remaining", fontWeight = FontWeight.Bold)
                val urgentTask = assignments.filter { it.deadline > System.currentTimeMillis() && it.deadline < System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000 }
                    .minByOrNull { it.deadline }
                if (urgentTask != null) {
                    Log.d(TAG, "Urgent task: $urgentTask")
                    Text("Upcoming urgent task: ${urgentTask.title} due tomorrow")
                }
            }
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: Assignment,
    onEditClick: (Assignment) -> Unit,
    onMarkAsCompleteClick: () -> Unit
) {
    val isOverdue = assignment.deadline < System.currentTimeMillis()
    val isComplete = assignment.progress == 100
    val urgencyColor by animateColorAsState(
        targetValue = when {
            isComplete -> Color(0xFF1E90FF)
            isOverdue -> Color(0xFFB00020)
            else -> getUrgencyColor(assignment.deadline)
        },
        label = "urgency_color"
    )
    val textColor = if (urgencyColor == Color(0xFFFFEE58) || isOverdue) Color.Black else Color.White
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = urgencyColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = assignment.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                IconButton(onClick = { onEditClick(assignment) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Assignment", tint = textColor)
                }
            }
            Text(text = assignment.subject, fontSize = 16.sp, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = getDeadlineText(assignment.deadline),
                    fontSize = 14.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Weightage: ${assignment.weightage}%", fontSize = 14.sp, color = textColor)
            }
            if (isOverdue) {
                val daysLeft = ((assignment.deadline - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                Text(text = "Overdue by ${abs(daysLeft)} days", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (assignment.attachmentUri != null) {
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(assignment.attachmentUri)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(intent)
                    }) {
                        Text("View Attachment")
                    }
                }
                if (assignment.progress < 100) {
                    Button(
                        onClick = onMarkAsCompleteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
                    ) {
                        Text("Mark as Complete")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { assignment.progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = textColor,
                trackColor = urgencyColor.copy(alpha = 0.4f)
            )
        }
    }
}
