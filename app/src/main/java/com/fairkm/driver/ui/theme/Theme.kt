package com.fairkm.driver.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF008577),
    secondary = Color(0xFFFFA000),
    tertiary = Color(0xFF00695C)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF55D6BE),
    secondary = Color(0xFFFFB74D),
    tertiary = Color(0xFF80CBC4)
)

@Composable
fun FairKMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors =
        if (darkTheme) {
            DarkColors
        } else {
            LightColors
        }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
