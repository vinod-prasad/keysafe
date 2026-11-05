package com.appnest.safe.credentials.vault.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.appnest.safe.credentials.vault.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
)

@Composable
fun KeySafeTheme(
    themeMode: String = "SYSTEM",
    fontStyle: String = "DEFAULT",
    fontSize: String = "DEFAULT",
    dynamicColor: Boolean = false, // Disabled to enforce the custom peach theme explicitly for now
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val fontFamily = when (fontStyle) {
        "Lato" -> FontFamily(Font(googleFont = GoogleFont("Lato"), fontProvider = provider))
        "Poppins" -> FontFamily(Font(googleFont = GoogleFont("Poppins"), fontProvider = provider))
        "Roboto" -> FontFamily(Font(googleFont = GoogleFont("Roboto"), fontProvider = provider))
        "Open Sans" -> FontFamily(Font(googleFont = GoogleFont("Open Sans"), fontProvider = provider))
        "Montserrat" -> FontFamily(Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider))
        else -> FontFamily.Default
    }

    val scale = when (fontSize) {
        "SMALL" -> 0.85f
        "MEDIUM" -> 1.15f
        "LARGE" -> 1.30f
        else -> 1.0f
    }

    fun TextStyle.applyScaleAndFont(family: FontFamily, scale: Float): TextStyle {
        return this.copy(
            fontFamily = family,
            fontSize = (this.fontSize.value * scale).sp,
            lineHeight = (this.lineHeight.value * scale).sp
        )
    }

    val customTypography = Typography.copy(
        displayLarge = Typography.displayLarge.applyScaleAndFont(fontFamily, scale),
        displayMedium = Typography.displayMedium.applyScaleAndFont(fontFamily, scale),
        displaySmall = Typography.displaySmall.applyScaleAndFont(fontFamily, scale),
        headlineLarge = Typography.headlineLarge.applyScaleAndFont(fontFamily, scale),
        headlineMedium = Typography.headlineMedium.applyScaleAndFont(fontFamily, scale),
        headlineSmall = Typography.headlineSmall.applyScaleAndFont(fontFamily, scale),
        titleLarge = Typography.titleLarge.applyScaleAndFont(fontFamily, scale),
        titleMedium = Typography.titleMedium.applyScaleAndFont(fontFamily, scale),
        titleSmall = Typography.titleSmall.applyScaleAndFont(fontFamily, scale),
        bodyLarge = Typography.bodyLarge.applyScaleAndFont(fontFamily, scale),
        bodyMedium = Typography.bodyMedium.applyScaleAndFont(fontFamily, scale),
        bodySmall = Typography.bodySmall.applyScaleAndFont(fontFamily, scale),
        labelLarge = Typography.labelLarge.applyScaleAndFont(fontFamily, scale),
        labelMedium = Typography.labelMedium.applyScaleAndFont(fontFamily, scale),
        labelSmall = Typography.labelSmall.applyScaleAndFont(fontFamily, scale),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content
    )
}