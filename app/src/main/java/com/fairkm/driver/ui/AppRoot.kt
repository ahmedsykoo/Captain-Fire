package com.fairkm.driver.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fairkm.driver.data.Platform
import com.fairkm.driver.data.PlatformConfig
import com.fairkm.driver.data.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun AppRoot(
    isAccessibilityEnabled: () -> Boolean,
    isOverlayEnabled: () -> Boolean,
    onRequestAccessibility: () -> Unit,
    onRequestOverlay: () -> Unit
) {
    var accessibilityOn by remember {
        mutableStateOf(isAccessibilityEnabled())
    }

    var overlayOn by remember {
        mutableStateOf(isOverlayEnabled())
    }

    LaunchedEffect(Unit) {
        accessibilityOn = isAccessibilityEnabled()
        overlayOn = isOverlayEnabled()
    }

    Scaffold { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding = PaddingValues(20.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            item {
                HeaderSection()
            }

            item {
                PermissionStatusCard(
                    accessibilityOn = accessibilityOn,
                    overlayOn = overlayOn,
                    onRequestAccessibility =
                        onRequestAccessibility,
                    onRequestOverlay =
                        onRequestOverlay
                )
            }

            item {
                Text(
                    text = "إعدادات المنصات",

                    style =
                        MaterialTheme.typography.titleLarge,

                    modifier =
                        Modifier.padding(top = 8.dp)
                )
            }

            items(
                Platform.values().toList()
            ) { platform ->

                PlatformConfigCard(
                    platform = platform
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column {

        Text(
            text = "Captain Fire",

            style =
                MaterialTheme.typography.headlineMedium,

            fontWeight =
                FontWeight.Bold
        )

        Text(
            text =
                "اعرف صافي الرحلة قبل ما تقبلها",

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun PermissionStatusCard(
    accessibilityOn: Boolean,
    overlayOn: Boolean,
    onRequestAccessibility: () -> Unit,
    onRequestOverlay: () -> Unit
) {
    Card(
        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text = "حالة التفعيل",

                style =
                    MaterialTheme.typography.titleMedium,

                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                Modifier.height(10.dp)
            )

            PermissionRow(
                title =
                    "خدمة قراءة الشاشة",

                enabled =
                    accessibilityOn,

                onClick =
                    onRequestAccessibility
            )

            Spacer(
                Modifier.height(8.dp)
            )

            PermissionRow(
                title =
                    "الظهور فوق التطبيقات",

                enabled =
                    overlayOn,

                onClick =
                    onRequestOverlay
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    if (enabled)
                        Icons.Filled.CheckCircle
                    else
                        Icons.Filled.Error,

                contentDescription = null,

                tint =
                    if (enabled)
                        Color(0xFF1E9E6D)
                    else
                        Color(0xFFD64545)
            )

            Text(
                text = title,

                style =
                    MaterialTheme.typography.bodyMedium,

                modifier =
                    Modifier.padding(start = 8.dp)
            )
        }

        if (!enabled) {
            TextButton(
                onClick = onClick
            ) {
                Text("تفعيل")
            }
        }
    }
}

@Composable
private fun PlatformConfigCard(
    platform: Platform
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var config by remember {
        mutableStateOf(
            PlatformConfig()
        )
    }

    var commissionText by remember {
        mutableStateOf("")
    }

    var targetText by remember {
        mutableStateOf("")
    }

    LaunchedEffect(platform) {

        SettingsRepository
            .observeConfig(
                context,
                platform
            )
            .collect { value ->

                config = value

                commissionText =
                    "%.2f".format(
                        value.companyCommissionPercent
                    )

                targetText =
                    "%.2f".format(
                        value.targetPricePerKm
                    )
            }
    }

    Card(
        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
                    platform.displayName,

                style =
                    MaterialTheme.typography.titleMedium,

                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                Modifier.height(10.dp)
            )

            OutlinedTextField(
                value =
                    commissionText,

                onValueChange = {
                    commissionText = it
                },

                label = {
                    Text(
                        "نسبة العمولة (%)"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                Modifier.height(8.dp)
            )

            OutlinedTextField(
                value =
                    targetText,

                onValueChange = {
                    targetText = it
                },

                label = {
                    Text(
                        "هدفك (ج/كم صافي)"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                Modifier.height(10.dp)
            )

            Button(
                onClick = {

                    val commission =
                        commissionText
                            .toDoubleOrNull()
                            ?: config.companyCommissionPercent

                    val target =
                        targetText
                            .toDoubleOrNull()
                            ?: config.targetPricePerKm

                    scope.launch {

                        SettingsRepository.saveConfig(
                            context,
                            platform,

                            config.copy(
                                companyCommissionPercent =
                                    commission,

                                targetPricePerKm =
                                    target
                            )
                        )

                        SettingsRepository
                            .setActivePlatform(
                                context,
                                platform
                            )
                    }
                },

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "حفظ واستخدام ${platform.displayName}"
                )
            }
        }
    }
}
