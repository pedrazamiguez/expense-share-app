package es.pedrazamiguez.splittrip.core.designsystem.foundation

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import es.pedrazamiguez.splittrip.core.designsystem.R

// OpenType feature tag for tabular (monospaced) numerals.
// Ensures decimal separators and currency digits align vertically in lists and the AnimatedAmount
// rolling animation renders without layout jitter.
private const val FONT_FEATURE_TABULAR_NUMS = "tnum"

// Plus Jakarta Sans — Display & Headline typeface ("Energetic Accents", Horizon Narrative §3)
val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold)
)

// Manrope — Title, Body & Label typeface ("Functional Grounding", Horizon Narrative §3)
val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

// Expressive Typography Scale — Option 2 "Modern Explorer" (Plus Jakarta Sans + Manrope)
val Typography = Typography(
    // DISPLAYS — Plus Jakarta Sans, tight letter-spacing (-0.02em) per Horizon Narrative §3
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-1.14).sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    displayMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.9).sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    displaySmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.72).sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),

    // HEADLINES — Plus Jakarta Sans, tight letter-spacing (-0.02em) per Horizon Narrative §3
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.64).sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.56).sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.48).sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),

    // TITLES — Manrope, tabular numerals for expense amounts and card headers
    titleLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    titleMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    titleSmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),

    // BODY — Manrope, tabular numerals for transaction lists, descriptions and currency details
    bodyLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    bodyMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    bodySmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),

    // LABELS — Manrope, tabular numerals for chips, badges and compact currency symbols
    labelLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    labelMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    ),
    labelSmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        fontFeatureSettings = FONT_FEATURE_TABULAR_NUMS
    )
)
