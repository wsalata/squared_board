package eu.tudek.squared_board.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.tudek.squared_board.ui.theme.Ink

/**
 * The prototype's icons are inline SVGs authored on a 24x24 grid; reusing their path data
 * keeps the line work identical. Parsing happens once per path string, not once per frame.
 */
@Composable
private fun rememberIconPath(pathData: String): Path =
    remember(pathData) { PathParser().parsePathString(pathData).toPath() }

private fun DrawScope.drawIcon(path: Path, stroke: Color?, fill: Color?, strokeWidth: Float) {
    val s = minOf(size.width, size.height) / 24f
    translate((size.width - 24f * s) / 2f, (size.height - 24f * s) / 2f) {
        scale(s, s, pivot = Offset.Zero) {
            fill?.let { drawPath(path, it) }
            stroke?.let {
                drawPath(path, it, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

@Composable
private fun VectorIcon(
    pathData: String,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    stroke: Color? = Ink.ink,
    fill: Color? = null,
    strokeWidth: Float = 2.4f,
) {
    val path = rememberIconPath(pathData)
    Canvas(modifier.size(size)) { drawIcon(path, stroke, fill, strokeWidth) }
}

/** Two paths drawn one over the other: a filled body with stroked detail on top. */
@Composable
private fun LayeredIcon(
    bodyData: String,
    detailData: String,
    modifier: Modifier,
    size: Dp,
    bodyFill: Color?,
    strokeWidth: Float = 2.4f,
) {
    val body = rememberIconPath(bodyData)
    val detail = rememberIconPath(detailData)
    Canvas(modifier.size(size)) {
        drawIcon(body, Ink.ink, bodyFill, strokeWidth)
        drawIcon(detail, Ink.ink, null, strokeWidth)
    }
}

@Composable
fun StarIcon(filled: Boolean, modifier: Modifier = Modifier, size: Dp = 22.dp) = VectorIcon(
    pathData = "M12 2.8l2.8 5.9 6.4.8-4.7 4.4 1.2 6.4L12 17.2l-5.7 3.1 1.2-6.4-4.7-4.4 6.4-.8z",
    modifier = modifier,
    size = size,
    stroke = Ink.ink,
    fill = if (filled) Ink.yellow else Ink.white,
    strokeWidth = 2f,
)

@Composable
fun SoundIcon(on: Boolean, modifier: Modifier = Modifier, size: Dp = 22.dp) = LayeredIcon(
    bodyData = "M4 9.5h3.5L12 5.5v13l-4.5-4H4z",
    detailData = if (on) "M15.5 9a4 4 0 010 6M18 6.5a7.5 7.5 0 010 11" else "M16 9.5l5 5M21 9.5l-5 5",
    modifier = modifier,
    size = size,
    bodyFill = Ink.ink,
)

@Composable
fun CloseIcon(modifier: Modifier = Modifier, size: Dp = 22.dp) =
    VectorIcon("M6 6l12 12M18 6L6 18", modifier, size, strokeWidth = 3f)

@Composable
fun BackIcon(modifier: Modifier = Modifier, size: Dp = 22.dp) =
    VectorIcon("M15 5l-7 7 7 7", modifier, size, strokeWidth = 3f)

@Composable
fun PlayIcon(modifier: Modifier = Modifier, size: Dp = 30.dp, tint: Color = Ink.white) =
    VectorIcon("M8 5.5v13l10.5-6.5z", modifier, size, stroke = tint, fill = tint, strokeWidth = 2f)

@Composable
fun StopwatchIcon(modifier: Modifier = Modifier, size: Dp = 30.dp) {
    val hands = rememberIconPath("M12 9.5v4l2.5 2M9.5 3h5")
    Canvas(modifier.size(size)) {
        val s = minOf(this.size.width, this.size.height) / 24f
        drawCircle(
            color = Ink.ink,
            radius = 7.5f * s,
            center = Offset(this.size.width / 2f, 13.5f * s + (this.size.height - 24f * s) / 2f),
            style = Stroke(width = 2.4f * s),
        )
        drawIcon(hands, Ink.ink, null, 2.4f)
    }
}

@Composable
fun BackspaceIcon(modifier: Modifier = Modifier, size: Dp = 30.dp) = LayeredIcon(
    bodyData = "M9 5h11v14H9l-6-7z",
    detailData = "M12 9.5l5 5M17 9.5l-5 5",
    modifier = modifier,
    size = size,
    bodyFill = null,
)
