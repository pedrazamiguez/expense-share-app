package es.pedrazamiguez.expenseshareapp.core.designsystem.foundation

import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope

// Dark Theme Color Scheme (Ocean Night)
private val DarkColorScheme = darkColorScheme(
    primary = OceanTealDark,
    onPrimary = Color(0xFF003731),
    primaryContainer = OceanTeal,
    onPrimaryContainer = Color.White,

    secondary = SlateBlueDark,
    onSecondary = Color(0xFF263238),
    secondaryContainer = SlateBlue,
    onSecondaryContainer = Color.White,

    tertiary = AdventureCoralDark,
    onTertiary = Color(0xFF3E2723),
    tertiaryContainer = AdventureCoral,
    onTertiaryContainer = Color.White,

    background = SurfaceNight,
    surface = SurfaceNight,
    onBackground = Color(0xFFE0E4E3),
    onSurface = Color(0xFFE0E4E3),

    // Surface container hierarchy for depth - tinted with teal
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,

    // Expressive containers - teal presence
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBEC9C6),

    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4947),

    error = Color(0xFFEF9A9A),
    onError = Color(0xFF690005)
)

// Light Theme Color Scheme (Tropical Day)
private val LightColorScheme = lightColorScheme(
    primary = OceanTeal,
    onPrimary = Color.White,
    primaryContainer = OceanTealLight,
    onPrimaryContainer = Color(0xFF00251A),

    secondary = SlateBlue,
    onSecondary = Color.White,
    secondaryContainer = SlateBlueContainer,
    onSecondaryContainer = Color(0xFF263238),

    tertiary = AdventureCoral,
    onTertiary = Color.White,
    tertiaryContainer = AdventureCoralContainer,
    onTertiaryContainer = Color(0xFF3E2723),

    background = SurfaceDay,
    surface = SurfaceDay,
    onBackground = Color(0xFF1A1C1C),
    onSurface = Color(0xFF1A1C1C),

    // Surface container hierarchy for depth - now tinted!
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,

    // Expressive containers - more teal presence
    surfaceVariant = Color(0xFFDAE5E3),     // Teal-tinted variant
    onSurfaceVariant = Color(0xFF3F4947),   // Darker, teal-influenced

    outline = Color(0xFF6F7977),            // Subtle teal in outlines
    outlineVariant = Color(0xFFBEC9C6),     // Lighter teal outline

    error = ErrorRed,
    onError = Color.White
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ExpenseShareAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We set dynamicColor to false by default to enforce our "Travel Brand" identity.
    // If true, it would use the user's wallpaper colors (Material You).
    dynamicColor: Boolean = false, content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        typography = Typography,
        motionScheme = MotionScheme.expressive(),
        content = {
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    content()
                }
            }
        }
    )

}
