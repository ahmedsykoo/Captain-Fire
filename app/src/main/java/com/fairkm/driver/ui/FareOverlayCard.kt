package com.fairkm.driver.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairkm.driver.data.FareOffer
import com.fairkm.driver.data.Platform
import com.fairkm.driver.data.PlatformConfig

@Composable
fun FareOverlayCard(
    offer: FareOffer?,
    config: PlatformConfig,
    platform: Platform,
    onDismiss: () -> Unit,
    onDrag: (Float, Float) -> Unit
) {
    AnimatedVisibility(
        visible = offer != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        val safeOffer = offer ?: return@AnimatedVisibility

        val netPerKm =
            safeOffer.netPricePerKm(
                config.companyCommissionPercent
            )

        val isGood =
            netPerKm >= config.targetPricePerKm

        val statusColor =
            if (isGood) {
                Color(0xFF1E9E6D)
            } else {
                Color(0xFFD64545)
            }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp,
            modifier = Modifier.width(230.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(
                                dragAmount.x,
                                dragAmount.y
                            )
                        }
                    }
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Filled.DragIndicator,
                            contentDescription = null,
                            tint =
                                MaterialTheme.colorScheme.outline,
                            modifier =
                                Modifier.height(18.dp)
                        )

                        Spacer(
                            Modifier.width(4.dp)
                        )

                        Text(
                            platform.displayName,
                            style =
                                MaterialTheme.typography.labelLarge
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier =
                            Modifier
                                .height(28.dp)
                                .width(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "إغلاق"
                        )
                    }
                }

                Spacer(
                    Modifier.height(8.dp)
                )

                Surface(
                    color =
                        statusColor.copy(alpha = 0.14f),
                    shape =
                        RoundedCornerShape(10.dp)
                ) {

                    Text(
                        text =
                            if (isGood) {
                                "سعر كويس"
                            } else {
                                "تحت الهدف"
                            },

                        color = statusColor,

                        fontWeight =
                            FontWeight.SemiBold,

                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            )
                    )
                }

                Spacer(
                    Modifier.height(10.dp)
                )

                Row(
                    verticalAlignment =
                        Alignment.Bottom
                ) {

                    Text(
                        text =
                            "%.2f".format(netPerKm),

                        fontSize = 30.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color = statusColor
                    )

                    Spacer(
                        Modifier.width(4.dp)
                    )

                    Text(
                        text = "ج/كم صافي",

                        style =
                            MaterialTheme.typography.bodyMedium,

                        modifier =
                            Modifier.padding(
                                bottom = 5.dp
                            )
                    )
                }

                Spacer(
                    Modifier.height(8.dp)
                )

                DetailRow(
                    "السعر المعروض",
                    "%.0f ج".format(
                        safeOffer.priceEgp
                    )
                )

                DetailRow(
                    "المسافة",
                    "%.1f كم".format(
                        safeOffer.distanceKm
                    )
                )

                DetailRow(
                    "العمولة",
                    "%.1f%%".format(
                        config.companyCommissionPercent
                    )
                )

                DetailRow(
                    "الصافي الكلي",
                    "%.0f ج".format(
                        safeOffer.netTotal(
                            config.companyCommissionPercent
                        )
                    )
                )

                DetailRow(
                    "هدفك",
                    "%.1f ج/كم".format(
                        config.targetPricePerKm
                    )
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        horizontalArrangement =
            Arrangement.SpaceBetween,

        modifier =
            Modifier
                .padding(vertical = 2.dp)
                .width(202.dp)
    ) {

        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.outline
        )

        Text(
            text = value,

            style =
                MaterialTheme.typography.bodyMedium,

            fontWeight =
                FontWeight.Medium
        )
    }
}
