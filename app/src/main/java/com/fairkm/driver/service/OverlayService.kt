package com.fairkm.driver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.fairkm.driver.data.FareBus
import com.fairkm.driver.data.FareOffer
import com.fairkm.driver.data.Platform
import com.fairkm.driver.data.PlatformConfig
import com.fairkm.driver.data.SettingsRepository
import com.fairkm.driver.ui.FareOverlayCard
import com.fairkm.driver.ui.theme.FairKMTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverlayService : Service(),
    LifecycleOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry =
        LifecycleRegistry(this)

    private val savedStateController =
        SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    private lateinit var windowManager: WindowManager

    private var composeView: ComposeView? = null

    private val scope =
        CoroutineScope(Dispatchers.Main + Job())

    private val currentOffer =
        mutableStateOf<FareOffer?>(null)

    private val currentConfig =
        mutableStateOf(PlatformConfig())

    private val currentPlatform =
        mutableStateOf(Platform.INDRIVE)

    private val fareListener: (FareOffer?) -> Unit = {
        currentOffer.value = it
    }

    override fun onCreate() {
        super.onCreate()

        savedStateController.performRestore(null)

        lifecycleRegistry.currentState =
            Lifecycle.State.CREATED

        startForegroundNotification()

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        FareBus.subscribe(fareListener)

        scope.launch {
            SettingsRepository
                .observeActivePlatform(this@OverlayService)
                .collectLatest { platform ->

                    currentPlatform.value = platform

                    SettingsRepository
                        .observeConfig(
                            this@OverlayService,
                            platform
                        )
                        .collectLatest { config ->
                            currentConfig.value = config
                        }
                }
        }

        addOverlayView()

        lifecycleRegistry.currentState =
            Lifecycle.State.RESUMED
    }

    private fun addOverlayView() {

        val overlayType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 180
        }

        val view = ComposeView(this).apply {

            setViewTreeLifecycleOwner(this@OverlayService)

            setViewTreeSavedStateRegistryOwner(
                this@OverlayService
            )

            setContent {

                FairKMTheme {

                    FareOverlayCard(
                        offer = currentOffer.value,
                        config = currentConfig.value,
                        platform = currentPlatform.value,

                        onDismiss = {
                            currentOffer.value = null
                        },

                        onDrag = { dx, dy ->

                            params.x += dx.toInt()
                            params.y += dy.toInt()

                            windowManager.updateViewLayout(
                                this,
                                params
                            )
                        }
                    )
                }
            }
        }

        composeView = view

        windowManager.addView(
            view,
            params
        )
    }

    private fun startForegroundNotification() {

        val channelId = "fairkm_overlay"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "FairKM",
                NotificationManager.IMPORTANCE_MIN
            )

            getSystemService(
                NOTIFICATION_SERVICE
            )
                .let {
                    it as NotificationManager
                }
                .createNotificationChannel(channel)
        }

        val notification: Notification =
            NotificationCompat.Builder(
                this,
                channelId
            )
                .setContentTitle("FairKM شغال")
                .setContentText("مراقبة عروض الرحلات")
                .setSmallIcon(
                    android.R.drawable.ic_menu_compass
                )
                .setPriority(
                    NotificationCompat.PRIORITY_MIN
                )
                .build()

        startForeground(
            1,
            notification
        )
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    override fun onDestroy() {

        FareBus.unsubscribe(
            fareListener
        )

        composeView?.let {
            runCatching {
                windowManager.removeView(it)
            }
        }

        lifecycleRegistry.currentState =
            Lifecycle.State.DESTROYED

        scope.coroutineContext[Job]?.cancel()

        super.onDestroy()
    }
}
