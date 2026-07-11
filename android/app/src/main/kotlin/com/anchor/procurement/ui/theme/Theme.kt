package com.anchor.procurement.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AnchorColors {
    val Charcoal = Color(0xFF131417)
    val CharcoalMid = Color(0xFF3A3B3F)
    val Primary = Color(0xFF55565B)
    val PrimaryDark = Color(0xFF303136)
    val Cream = Color(0xFFFAF9F5)
    val Surface = Color(0xFFFFFFFF)
    val Outline = Color(0xFFDDD3C8)
    val OutlineFaint = Color(0xFFEDE6DC)
    val TextMuted = Color(0xFF8A7A6E)
    val TextWarm = Color(0xFF3D2B21)
    val TextWarmMuted = Color(0xFF523C2C)
    val Success = Color(0xFF8FAC78)
    val SuccessDark = Color(0xFF5E7A4C)
    val Warn = Color(0xFFD0A050)
    val Danger = Color(0xFFB85C48)
    val ChipBg = Color(0xFFF3EEE6)

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
    secondaryContainer = Color(0xFFEDE3D6),
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

private val AnchorTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.sp),
)

@Composable
fun AnchorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AnchorTypography,
        content = content,
    )
}
