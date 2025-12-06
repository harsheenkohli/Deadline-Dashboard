package com.example.deadlinedashboard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subject: String,
    val title: String,
    val deadline: Long,
    val weightage: Int,
    val status: String,
    val progress: Int = 0,
    val attachmentUri: String? = null,
    val reminderOffset: Long? = null, // Time in millis before deadline to send reminder
    val completionDate: Long? = null
)
