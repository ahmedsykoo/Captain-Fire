package com.fairkm.driver.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.fairkm.driver.service.FareAccessibilityService

object PermissionUtils {

    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponent = ComponentName(
            context,
            FareAccessibilityService::class.java
        )

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices
            .split(':')
            .any {
                ComponentName.unflattenFromString(it) == expectedComponent
            }
    }
}
