package com.example.deadlinedashboard.ui.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.example.deadlinedashboard.data.model.Assignment
import com.example.deadlinedashboard.viewmodel.AssignmentViewModel

class DeadlineWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // This is where you would fetch your data
        // For now, we'll use a placeholder
        val assignments = emptyList<Assignment>()

        provideContent {
            DeadlineWidgetContent(assignments)
        }
    }

    @Composable
    private fun DeadlineWidgetContent(assignments: List<Assignment>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Upcoming Deadlines")
            if (assignments.isNotEmpty()) {
                assignments.take(2).forEach {
                    Text("${it.title} - Due on ${it.deadline}")
                }
            } else {
                Text("No upcoming deadlines")
            }
        }
    }
}
