package com.anchor.procurement.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.anchor.procurement.R

// Bundled font files, not system fonts — the exact typefaces from the original
// design must render identically on every device regardless of what's installed.
val GordenFamily = FontFamily(Font(R.font.gorden, FontWeight.Normal), Font(R.font.gorden, FontWeight.Bold))
val AvianoSerifFamily = FontFamily(Font(R.font.aviano_serif, FontWeight.Normal), Font(R.font.aviano_serif, FontWeight.Bold))
val AvianoGothicFamily = FontFamily(Font(R.font.aviano_gothic, FontWeight.Normal), Font(R.font.aviano_gothic, FontWeight.Bold))
val GaleyFamily = FontFamily(Font(R.font.galey_regular, FontWeight.Normal), Font(R.font.galey_bold, FontWeight.Bold))

object AnchorColors {
    val Charcoal = Color(0xFF131417)
    val CharcoalMid = Color(0xFF3A3B3F)
    val Primary = Color(0xFF55565B)
    val PrimaryDark = Color(0xFF303136)
    val Cream = Color(0xFFFAF9F5)
    val Surface = Color(0xFFFFFFFF)
    val Outline = Color(0xFFD2D3D8)
    val OutlineFaint = Color(0xFFE4E5E9)
    val TextMuted = Color(0xFFA6A7AC)
    val TextWarm = Color(0xFF3D2B21)
    val TextWarmMuted = Color(0xFF523C2C)
    val Success = Color(0xFF8FAC78)
    val SuccessDark = Color(0xFF5E7A4C)
    val Warn = Color(0xFFD0A050)
    val Danger = Color(0xFFB85C48)
    val ChipBg = Color(0xFFF5F5F7)

    val categoryPalette = listOf(
        Color(0xFF5B7B9A), Color(0xFF8FAC78), Color(0xFFC99B5D), Color(0xFFA56A5F), Color(0xFF77787D),
    )
}

private val LightColors = lightColorScheme(
    primary = AnchorColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AnchorColors.Primary,
    onPrimaryContainer = Color.White,
    secondary = AnchorColors.Primary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7E7EA),
    onSecondaryContainer = AnchorColors.Primary,
    tertiary = AnchorColors.SuccessDark,
    tertiaryContainer = Color(0xFFDCE8D3),
    onTertiaryContainer = AnchorColors.SuccessDark,
    background = AnchorColors.Cream,
    onBackground = AnchorColors.CharcoalMid,
    surface = AnchorColors.Surface,
    onSurface = AnchorColors.CharcoalMid,
    surfaceVariant = AnchorColors.ChipBg,
    onSurfaceVariant = AnchorColors.TextMuted,
    error = AnchorColors.Danger,
    outline = AnchorColors.Outline,
    // Material3 auto-tints elevated surfaces (Card, TopAppBar, NavigationBar) toward this
    // color for tonal elevation. Left at its default (= primary), every white card and
    // app bar was picking up a grey cast from our charcoal primary. Disable that tinting
    // so explicit surface/card colors stay the color we actually set.
    surfaceTint = Color.Transparent,
)

// Every Material3 type-scale role gets an explicit bundled-font TextStyle so that
// even styles this app doesn't currently use (headline*, display*, titleSmall) still
// render in the locked typefaces if referenced later — none of them fall through to
// Typography()'s system-font (Roboto) defaults.
// Sizes below are ~15% smaller than the Material3 defaults per user request.
private val AnchorTypography = Typography(
    displayLarge = TextStyle(fontFamily = AvianoSerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 38.sp),
    displayMedium = TextStyle(fontFamily = AvianoSerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 31.sp),
    displaySmall = TextStyle(fontFamily = AvianoSerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 27.sp),
    headlineLarge = TextStyle(fontFamily = AvianoSerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 26.sp),
    headlineMedium = TextStyle(fontFamily = AvianoSerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    headlineSmall = TextStyle(fontFamily = AvianoSerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleLarge = TextStyle(fontFamily = AvianoSerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 19.sp),
    titleMedium = TextStyle(fontFamily = AvianoSerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    titleSmall = TextStyle(fontFamily = AvianoSerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    bodyLarge = TextStyle(fontFamily = AvianoGothicFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 19.sp),
    bodyMedium = TextStyle(fontFamily = AvianoGothicFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    bodySmall = TextStyle(fontFamily = AvianoGothicFamily, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 14.sp),
    labelLarge = TextStyle(fontFamily = AvianoGothicFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelMedium = TextStyle(fontFamily = AvianoGothicFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp),
    labelSmall = TextStyle(fontFamily = AvianoGothicFamily, fontWeight = FontWeight.Medium, fontSize = 9.sp, letterSpacing = 1.sp),
)

@Composable
fun AnchorTheme(content: @Composable () -> Unit) {
    // Pin fontScale to 1 so the system-wide "Font size" accessibility setting can't
    // resize app text — the sp values above are the only thing controlling size.
    val fixedDensity = Density(density = LocalDensity.current.density, fontScale = 1f)
    CompositionLocalProvider(LocalDensity provides fixedDensity) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = AnchorTypography,
            content = content,
        )
    }
}
