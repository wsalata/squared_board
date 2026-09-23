package eu.tudek.squared_board.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import eu.tudek.squared_board.R

/** The paper-and-ink palette lifted straight from the web prototype. */
object Ink {
    val paper = Color(0xFFFBFDFF)
    val grid = Color(0xFFDDE9F5)
    val ink = Color(0xFF1D2C5C)
    val inkSoft = Color(0xFF56648C)
    val red = Color(0xFFF2545B)
    val redSoft = Color(0xFFFFE1E2)
    val yellow = Color(0xFFFFC53D)
    val green = Color(0xFF2DB67C)
    val greenSoft = Color(0xFFDDF5E9)
    val blue = Color(0xFF3B8BEB)
    val white = Color(0xFFFFFFFF)

    /** Tile shades for mastery levels 0..5. */
    val tiers = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFEDB8),
        Color(0xFFFFD464),
        Color(0xFFA9E4C5),
        Color(0xFF62CC98),
        Color(0xFF2DB67C),
    )

    /** Text colour that stays readable on top of [tiers]. */
    fun onTier(level: Int) = if (level >= 4) white else if (level > 0) ink else inkSoft
}

/**
 * Baloo 2 gives the prototype its rounded, hand-drawn voice. It ships as a single variable
 * font, so each weight is one axis setting rather than a separate file.
 */
private fun balooWeight(weight: FontWeight) = Font(
    R.font.baloo2,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val AppFontFamily = FontFamily(
    balooWeight(FontWeight.Medium),
    balooWeight(FontWeight.SemiBold),
    balooWeight(FontWeight.Bold),
    balooWeight(FontWeight.ExtraBold),
)

private val base = TextStyle(fontFamily = AppFontFamily, color = Ink.ink)

object AppType {
    val h1 = base.copy(fontSize = 32.sp, lineHeight = 33.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
    val h2 = base.copy(fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
    val body = base.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
    val bodyBold = base.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
    val label = base.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
    val button = base.copy(fontSize = 22.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold)
    val digits = base.copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
fun SquaredBoardTheme(content: @Composable () -> Unit) {
    // The notebook look is deliberately light-only, as in the prototype.
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Ink.blue,
            background = Ink.paper,
            surface = Ink.white,
            onBackground = Ink.ink,
            onSurface = Ink.ink,
        ),
        typography = MaterialTheme.typography.copy(
            bodyLarge = AppType.body,
            bodyMedium = AppType.body,
            labelLarge = AppType.label,
        ),
        content = content,
    )
}
