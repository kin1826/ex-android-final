package com.gamestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.gamestore.ui.AppNavigation
import com.gamestore.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Không dùng enableEdgeToEdge() — gây ra khoảng trống
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            AppTheme { AppNavigation() }
        }
    }
}