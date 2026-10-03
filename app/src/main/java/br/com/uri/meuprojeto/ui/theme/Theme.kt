package br.com.uri.meuprojeto.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CampusIndigoDark,
    onPrimary = OnCampusIndigoDark,
    primaryContainer = CampusIndigoContainerDark,
    onPrimaryContainer = OnCampusIndigoContainerDark,
    secondary = CampusSecondaryDark,
    onSecondary = OnCampusSecondaryDark,
    secondaryContainer = CampusSecondaryContainerDark,
    onSecondaryContainer = OnCampusSecondaryContainerDark,
    tertiary = CampusTealDark,
    onTertiary = OnCampusTealDark,
    tertiaryContainer = CampusTealContainerDark,
    onTertiaryContainer = OnCampusTealContainerDark,
    background = CampusBackgroundDark,
    onBackground = CampusOnBackgroundDark,
    surface = CampusSurfaceDark,
    onSurface = CampusOnSurfaceDark,
    surfaceVariant = CampusSurfaceVariantDark,
    onSurfaceVariant = CampusOnSurfaceVariantDark,
    outline = CampusOutlineDark,
    outlineVariant = CampusOutlineVariantDark,
    error = CampusErrorDark,
    onError = CampusOnErrorDark,
    errorContainer = CampusErrorContainerDark,
    onErrorContainer = CampusOnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = CampusIndigo,
    onPrimary = OnCampusIndigo,
    primaryContainer = CampusIndigoContainer,
    onPrimaryContainer = OnCampusIndigoContainer,
    secondary = CampusSecondary,
    onSecondary = OnCampusSecondary,
    secondaryContainer = CampusSecondaryContainer,
    onSecondaryContainer = OnCampusSecondaryContainer,
    tertiary = CampusTeal,
    onTertiary = OnCampusTeal,
    tertiaryContainer = CampusTealContainer,
    onTertiaryContainer = OnCampusTealContainer,
    background = CampusBackground,
    onBackground = CampusOnBackground,
    surface = CampusSurface,
    onSurface = CampusOnSurface,
    surfaceVariant = CampusSurfaceVariant,
    onSurfaceVariant = CampusOnSurfaceVariant,
    outline = CampusOutline,
    outlineVariant = CampusOutlineVariant,
    error = CampusError,
    onError = CampusOnError,
    errorContainer = CampusErrorContainer,
    onErrorContainer = CampusOnErrorContainer
)

@Immutable
data class CampusHubExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color
)

private val LightExtendedColors = CampusHubExtendedColors(
    success = CampusSuccess,
    onSuccess = CampusOnSuccess,
    successContainer = CampusSuccessContainer,
    onSuccessContainer = CampusOnSuccessContainer
)

private val DarkExtendedColors = CampusHubExtendedColors(
    success = CampusSuccessDark,
    onSuccess = CampusOnSuccessDark,
    successContainer = CampusSuccessContainerDark,
    onSuccessContainer = CampusOnSuccessContainerDark
)

private val LocalCampusHubExtendedColors = staticCompositionLocalOf {
    LightExtendedColors
}

val MaterialTheme.campusHubColors: CampusHubExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCampusHubExtendedColors.current

@Composable
fun MeuProjetoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalCampusHubExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = CampusHubShapes,
            content = content
        )
    }
}
