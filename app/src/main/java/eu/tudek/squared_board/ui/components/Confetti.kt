package eu.tudek.squared_board.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private val CONFETTI_COLORS = listOf(
    Color(0xFFFFC53D),
    Color(0xFF2DB67C),
    Color(0xFF3B8BEB),
    Color(0xFFF2545B),
    Color(0xFF1D2C5C),
)

private const val TOTAL_MS = 3500
private const val PIECES = 60

private class Piece(
    val xFraction: Float,
    val color: Color,
    val delayMs: Float,
    val durationMs: Float,
    val round: Boolean,
    val spin: Float,
)

/**
 * The celebratory shower from the prototype: squares and circles tumbling past the viewport,
 * each with its own delay and fall speed. Fires once, then reports back so it can be removed.
 */
@Composable
fun Confetti(onFinished: () -> Unit) {
    val pieces = remember {
        List(PIECES) { k ->
            Piece(
                xFraction = Random.nextFloat(),
                color = CONFETTI_COLORS[k % CONFETTI_COLORS.size],
                delayMs = Random.nextFloat() * 700f,
                durationMs = 1800f + Random.nextFloat() * 1200f,
                round = k % 3 == 0,
                spin = if (Random.nextBoolean()) 720f else -720f,
            )
        }
    }
    val clock = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        clock.animateTo(1f, tween(durationMillis = TOTAL_MS, easing = LinearEasing))
        onFinished()
    }

    Canvas(Modifier.fillMaxSize()) {
        val elapsed = clock.value * TOTAL_MS
        val side = 12.dp.toPx()
        val start = -20.dp.toPx()
        pieces.forEach { piece ->
            val t = ((elapsed - piece.delayMs) / piece.durationMs)
            if (t <= 0f || t >= 1f) return@forEach
            // Gravity, roughly: the CSS animation used an ease-in curve over the whole fall.
            val eased = t * t
            val x = piece.xFraction * size.width
            val y = start + eased * (size.height * 1.1f - start)
            rotate(degrees = piece.spin * t, pivot = Offset(x + side / 2f, y + side / 2f)) {
                if (piece.round) {
                    drawCircle(piece.color, radius = side / 2f, center = Offset(x + side / 2f, y + side / 2f))
                } else {
                    drawRoundRect(
                        piece.color,
                        topLeft = Offset(x, y),
                        size = Size(side, side),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                    )
                }
            }
        }
    }
}
