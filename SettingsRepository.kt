package com.fairkm.driver.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "fairkm_settings")

/**
 * Supported ride-hailing platforms. Each has its own official commission %
 * (the cut the platform itself takes from the driver's fare) plus the
 * driver's own target price/km, so net profit can be shown accurately.
 */
enum class Platform(val displayName: String) {
    INDRIVE("inDrive"),
    DIDI("Didi"),
    UBER("Uber"),
    SWVL("Swvl")
}

data class PlatformConfig(
    val targetPricePerKm: Double = 6.0,
    val companyCommissionPercent: Double = 10.83
)

object SettingsRepository {

    private fun commissionKey(p: Platform) = doublePreferencesKey("${p.name}_commission")
    private fun targetKey(p: Platform) = doublePreferencesKey("${p.name}_target")

    private val ACTIVE_PLATFORM = stringPreferencesKey("active_platform")
    private val OVERLAY_ENABLED = stringPreferencesKey("overlay_enabled")

    fun observeConfig(context: Context, platform: Platform): Flow<PlatformConfig> {
        return context.dataStore.data.map { prefs ->
            PlatformConfig(
                targetPricePerKm = prefs[targetKey(platform)] ?: defaultTarget(platform),
                companyCommissionPercent = prefs[commissionKey(platform)] ?: defaultCommission(platform)
            )
        }
    }

    suspend fun saveConfig(context: Context, platform: Platform, config: PlatformConfig) {
        context.dataStore.edit { prefs ->
            prefs[targetKey(platform)] = config.targetPricePerKm
            prefs[commissionKey(platform)] = config.companyCommissionPercent
        }
    }

    fun observeActivePlatform(context: Context): Flow<Platform> {
        return context.dataStore.data.map { prefs ->
            val name = prefs[ACTIVE_PLATFORM] ?: Platform.INDRIVE.name
            runCatching { Platform.valueOf(name) }.getOrDefault(Platform.INDRIVE)
        }
    }

    suspend fun setActivePlatform(context: Context, platform: Platform) {
        context.dataStore.edit { it[ACTIVE_PLATFORM] = platform.name }
    }

    private fun defaultCommission(platform: Platform): Double = when (platform) {
        Platform.INDRIVE -> 10.83
        Platform.DIDI -> 15.0
        Platform.UBER -> 20.0
        Platform.SWVL -> 12.0
    }

    private fun defaultTarget(platform: Platform): Double = 6.0
}
