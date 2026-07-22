package com.proporit.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "change_events")
data class ChangeEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMillis: Long,
    val onTime: Boolean,
)
