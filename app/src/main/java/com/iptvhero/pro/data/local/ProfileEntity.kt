package com.iptvhero.pro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val sourceType: String = "URL",
    val isActive: Boolean = false
)
