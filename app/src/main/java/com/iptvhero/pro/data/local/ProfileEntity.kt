package com.iptvhero.pro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SourceType {
    XTREAM, M3U, TXT
}

enum class ExpiryStatus {
    VALID, EXPIRING_SOON, EXPIRED, UNLIMITED
}

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val heroName: String,
    val sourceType: String,
    val server: String,
    val username: String = "",
    val password: String = "",
    val expiryDate: Long = 0L,
    val maxConnections: Int = 1,
    val liveCount: Int = 0,
    val vodCount: Int = 0,
    val seriesCount: Int = 0,
    val isActive: Boolean = false,
    val lastUsed: Long = System.currentTimeMillis()
) {
    fun getContentCountsSummary(): String =
        "Ao Vivo: $liveCount | Filmes: $vodCount | Séries: $seriesCount"

    fun getExpiryStatus(): ExpiryStatus {
        if (expiryDate <= 0L) return ExpiryStatus.UNLIMITED
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
        return when {
            expiryDate < now -> ExpiryStatus.EXPIRED
            expiryDate - now <= sevenDaysMs -> ExpiryStatus.EXPIRING_SOON
            else -> ExpiryStatus.VALID
        }
    }

    fun getFormattedExpiry(): String {
        if (expiryDate <= 0L) return "Ilimitado / N/A"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(expiryDate))
    }
}
