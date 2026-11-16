package com.iptvhero.pro.data.local
package com.iptvhero.pro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val sourceType: String = "URL",
    val isActive: Boolean = false // <--- Essencial para corresponder ao ProfileDao e passar no KSP
) 
{
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
