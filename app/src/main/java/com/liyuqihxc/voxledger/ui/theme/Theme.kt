package com.liyuqihxc.voxledger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenOnContainer,
    secondary = AmberAccent,
    secondaryContainer = AmberContainer,
)

private val DarkColors = darkColorScheme(
    primary = GreenContainer,
    onPrimary = GreenOnContainer,
    primaryContainer = GreenPrimary,
    onPrimaryContainer = GreenOnPrimary,
    secondary = AmberContainer,
)

@Composable
fun VoxLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
