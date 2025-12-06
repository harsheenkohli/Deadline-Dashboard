package com.example.deadlinedashboard.ui.add_assignment

import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.deadlinedashboard.data.model.Assignment
import com.example.deadlinedashboard.utils.NotificationScheduler
import com.example.deadlinedashboard.viewmodel.AssignmentViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ReminderOption(val label: String, val offset: Long?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentSheet(
    viewModel: AssignmentViewModel,
    assignment: Assignment?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val TAG = "AddAssignmentSheet"

    // State for all the fields in the sheet
    var subject by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Long?>(null) }
    var weightage by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0) }
    var attachmentUri by remember { mutableStateOf<String?>(null) }
    var selectedReminder by remember { mutableStateOf<ReminderOption?>(null) }

    val reminderOptions = listOf(
        ReminderOption("No reminder", null),
        ReminderOption("1 hour before", TimeUnit.HOURS.toMillis(1)),
        ReminderOption("1 day before", TimeUnit.DAYS.toMillis(1)),
        ReminderOption("3 days before", TimeUnit.DAYS.toMillis(3))
    )

    // Activity launcher for picking a file
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            attachmentUri = it.data?.data?.toString()
            Log.d(TAG, "File attached: $attachmentUri")
        }
    }

    // Pre-fill fields if an assignment is being edited
    LaunchedEffect(assignment) {
        if (assignment != null) {
            Log.d(TAG, "Editing assignment: $assignment")
            subject = assignment.subject
            title = assignment.title
            deadline = assignment.deadline
            weightage = assignment.weightage.toString()
            progress = assignment.progress
            attachmentUri = assignment.attachmentUri
            selectedReminder = reminderOptions.find { it.offset == assignment.reminderOffset }
        } else {
            Log.d(TAG, "Adding new assignment")
        }
    }

    val isFormValid by remember(subject, title, deadline, weightage) {
        mutableStateOf(subject.isNotBlank() && title.isNotBlank() && deadline != null && weightage.isNotBlank() && weightage.toIntOrNull() != null)
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    deadline?.let { calendar.timeInMillis = it }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            Log.d(TAG, "Time selected: $hourOfDay:$minute")
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            deadline = calendar.timeInMillis
        },
        23,
        59,
        false
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            Log.d(TAG, "Date selected: $year-${month + 1}-$dayOfMonth")
            calendar.set(year, month, dayOfMonth)
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(if (assignment == null) "Add New Assignment" else "Edit Assignment")
            Spacer(modifier = Modifier.height(16.dp))

            // Input Fields
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, placeholder = { Text("e.g., Computer Science") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, placeholder = { Text("e.g., Final Project") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            // Deadline Picker
            Box {
                OutlinedTextField(
                    value = deadline?.let { SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault()).format(it) } ?: "",
                    onValueChange = {},
                    label = { Text("Deadline") },
                    placeholder = { Text("Select a date and time") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Date") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Box(modifier = Modifier.matchParentSize().clickable { datePickerDialog.show() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = weightage, onValueChange = { weightage = it }, label = { Text("Weightage (%)") }, placeholder = { Text("e.g., 25") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            // Progress Slider
            Spacer(modifier = Modifier.height(16.dp))
            Text("Progress: $progress%")
            Slider(value = progress.toFloat(), onValueChange = { progress = it.toInt() }, valueRange = 0f..100f, steps = 100)

            // Reminder Dropdown
            var expanded by remember { mutableStateOf(false) }
            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedReminder?.label ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Reminder") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    reminderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                selectedReminder = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Attachment Button
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }
                    launcher.launch(intent)
                }) { Text("Attach File") }
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                attachmentUri?.let { Text(Uri.parse(it).lastPathSegment ?: "File Attached") }
            }

            // Save/Update Button
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    Log.d(TAG, "Add/Update button clicked")
                    val weightageInt = try {
                        weightage.toInt()
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Invalid weightage format: '$weightage'", e)
                        Toast.makeText(context, "Invalid weightage", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val updatedAssignment = Assignment(
                        id = assignment?.id ?: 0,
                        subject = subject,
                        title = title,
                        deadline = deadline ?: 0L,
                        weightage = weightageInt,
                        status = if (progress == 100) "Done" else "Pending",
                        progress = progress,
                        attachmentUri = attachmentUri,
                        reminderOffset = selectedReminder?.offset
                    )
                    Log.d(TAG, "Saving assignment: $updatedAssignment")
                    if (assignment == null) {
                        Log.d(TAG, "Inserting new assignment")
                        viewModel.insert(updatedAssignment)
                    } else {
                        Log.d(TAG, "Updating existing assignment")
                        viewModel.update(updatedAssignment)
                    }

                    if (updatedAssignment.reminderOffset != null) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                            Log.d(TAG, "Cannot schedule exact alarms")
                            Toast.makeText(context, "Please grant permission to schedule exact alarms", Toast.LENGTH_SHORT).show()
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
                                context.startActivity(it)
                            }
                        } else {
                            Log.d(TAG, "Scheduling notification")
                            NotificationScheduler.scheduleNotification(context, updatedAssignment)
                        }
                    } else {
                        assignment?.let {
                            Log.d(TAG, "Cancelling notification")
                            NotificationScheduler.cancelNotification(context, it)
                        }
                    }

                    Log.d(TAG, "Dismissing sheet")
                    onDismiss()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (assignment == null) "Add Assignment" else "Update Assignment")
            }
        }
    }
}