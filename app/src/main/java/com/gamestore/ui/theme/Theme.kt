package com.gamestore.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val Purple     = Color(0xFF7C3AED)
val PurpleLt   = Color(0xFFA855F7)
val DarkBg     = Color(0xFF09090F)
val DarkSurf   = Color(0xFF0D0D1F)
val DarkCard   = Color(0xFF1A1A2E)
val DarkBorder = Color(0xFF2A2A3E)
val TextPri    = Color(0xFFE2E8F0)
val TextMuted  = Color(0xFF94A3B8)
val RedColor   = Color(0xFFF43F5E)
val GreenColor = Color(0xFF10B981)
val AmberColor = Color(0xFFFBBF24)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary          = PurpleLt,
            onPrimary        = Color.White,
            background       = DarkBg,
            onBackground     = TextPri,
            surface          = DarkSurf,
            onSurface        = TextPri,
            surfaceVariant   = DarkCard,
            onSurfaceVariant = TextMuted,
            outline          = DarkBorder,
            error            = RedColor,
        ),
        content = content,
    )
}