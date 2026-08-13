package com.fairkm.driver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fairkm.driver.ui.AppRoot
import com.fairkm.driver.ui.theme.FairKMTheme
import com.fairkm.driver.util.PermissionUtils

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            FairKMTheme {
                AppRoot(
                    isAccessibilityEnabled = {
                        PermissionUtils.isAccessibilityServiceEnabled(this)
                    },
                    isOverlayEnabled = {
                        PermissionUtils.canDrawOverlays(this)
                    },
                    onRequestAccessibility = {
                        startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        )
                    },
                    onRequestOverlay = {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                        )
                    }
                )
            }
        }
    }
}
