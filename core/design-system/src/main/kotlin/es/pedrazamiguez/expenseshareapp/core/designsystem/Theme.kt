package es.pedrazamiguez.expenseshareapp.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Dark Theme Color Scheme (Ocean Night)
private val DarkColorScheme = darkColorScheme(
    primary = OceanTealDark,
    onPrimary = Color(0xFF00363D),
    primaryContainer = OceanTeal,
    onPrimaryContainer = Color.White, // High contrast on teal

    secondary = SlateBlueDark,
    onSecondary = Color(0xFF1B3439),
    secondaryContainer = SlateBlue,
    onSecondaryContainer = Color.White,

    tertiary = AdventureCoralDark, // Pop color
    onTertiary = Color(0xFF4E2616),
    tertiaryContainer = AdventureCoral,
    onTertiaryContainer = Color(0xFF33150B),

    background = SurfaceNight,
    surface = SurfaceNight,
    onBackground = Color(0xFFE0E3E3),
    onSurface = Color(0xFFE0E3E3),

    // Expressive containers
    surfaceVariant = Color(0xFF3F484A),
    onSurfaceVariant = Color(0xFFBFC8CA),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// Light Theme Color Scheme (Tropical Day)
private val LightColorScheme = lightColorScheme(
    primary = OceanTeal,
    onPrimary = Color.White,
    primaryContainer = OceanTealLight,
    onPrimaryContainer = Color(0xFF001F24),

    secondary = SlateBlue,
    onSecondary = Color.White,
    secondaryContainer = SlateBlueContainer,
    onSecondaryContainer = Color(0xFF051F23),

    tertiary = AdventureCoral, // Pop color
    onTertiary = Color.White,
    tertiaryContainer = AdventureCoralContainer,
    onTertiaryContainer = Color(0xFF33150B),

    background = SurfaceDay,
    surface = SurfaceDay,
    onBackground = Color(0xFF191C1D),
    onSurface = Color(0xFF191C1D),

    // Expressive containers
    surfaceVariant = Color(0xFFDBE4E6),
    onSurfaceVariant = Color(0xFF3F484A),

    error = ErrorRed,
    onError = Color.White
)

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes, // Apply our expressive shapes
        content = content
    )
}
