package com.iptvhero.pro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExpiryStatus {
    ACTIVE, EXPIRED
}

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sourceType: String = "URL",
    val expiryDate: Long? = null, // Timestamp em milissegundos
    val isActive: Boolean = false
) {
    fun getFormattedExpiry(): String {
        return if (expiryDate != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(expiryDate))
        } else {
            "Vitalício / Sem Expiração"
        }
    }

    fun getExpiryStatus(): ExpiryStatus {
        if (expiryDate == null) return ExpiryStatus.ACTIVE
        val diff = expiryDate - System.currentTimeMillis()
        return if (diff < 0) ExpiryStatus.EXPIRED else ExpiryStatus.ACTIVE
    }
}
