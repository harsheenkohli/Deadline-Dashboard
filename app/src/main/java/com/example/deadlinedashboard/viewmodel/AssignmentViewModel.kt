package com.example.deadlinedashboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deadlinedashboard.data.db.AppDatabase
import com.example.deadlinedashboard.data.model.Assignment
import com.example.deadlinedashboard.repository.AssignmentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class AssignmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AssignmentRepository
    val allAssignments: StateFlow<List<Assignment>>

    init {
        val assignmentDao = AppDatabase.getDatabase(application).assignmentDao()
        repository = AssignmentRepository(assignmentDao)
        allAssignments = repository.allAssignments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insert(assignment: Assignment) = viewModelScope.launch {
        repository.insert(assignment)
    }

    fun update(assignment: Assignment) = viewModelScope.launch {
        repository.update(assignment)
    }

    fun delete(assignment: Assignment) = viewModelScope.launch {
        repository.delete(assignment)
    }

    fun resetWeek() = viewModelScope.launch {
        val today = Calendar.getInstance()
        val startOfWeek = today.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_WEEK, 6)

        val pendingAssignments = allAssignments.first().filter { 
            it.progress < 100 && it.deadline >= startOfWeek.timeInMillis && it.deadline <= endOfWeek.timeInMillis
        }

        pendingAssignments.forEach { assignment ->
            val newDeadline = (assignment.deadline + 7 * 24 * 60 * 60 * 1000)
            update(assignment.copy(deadline = newDeadline))
        }
    }
}
