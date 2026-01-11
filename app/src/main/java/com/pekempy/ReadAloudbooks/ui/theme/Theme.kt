package com.pekempy.ReadAloudbooks.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun ReadAloudBooksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColour: Boolean = true,
    themeSource: Int = 0,
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColour && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val seedColor = when {
                themeSource == 0 -> Purple40
                themeSource in 1..4 -> when(themeSource) {
                    1 -> androidx.compose.ui.graphics.Color(0xFF2196F3) 
                    2 -> androidx.compose.ui.graphics.Color(0xFFF44336) 
                    3 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) 
                    4 -> androidx.compose.ui.graphics.Color(0xFFFF9800) 
                    else -> Purple40 
                }
                else -> androidx.compose.ui.graphics.Color(themeSource)
            }
            
            if (darkTheme) {
                 darkColorScheme(
                    primary = seedColor,
                    secondary = seedColor,
                    tertiary = seedColor
                 )
            } else {
                 lightColorScheme(
                    primary = seedColor,
                    secondary = seedColor,
                    tertiary = seedColor
                 )
            }
        }
    }.let {
        if (amoled && darkTheme) {
            it.copy(
                background = androidx.compose.ui.graphics.Color.Black,
                surface = androidx.compose.ui.graphics.Color.Black,
                surfaceContainer = androidx.compose.ui.graphics.Color.Black,
                surfaceContainerHigh = androidx.compose.ui.graphics.Color.Black,
                surfaceContainerHighest = androidx.compose.ui.graphics.Color.Black,
                surfaceContainerLow = androidx.compose.ui.graphics.Color.Black,
                surfaceContainerLowest = androidx.compose.ui.graphics.Color.Black,
            )
        } else {
            it
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
