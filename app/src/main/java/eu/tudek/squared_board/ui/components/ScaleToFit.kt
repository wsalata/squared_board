package eu.tudek.squared_board.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

/**
 * Measures [content] at its natural width and scales it down if it overflows.
 *
 * The prototype shrank the equation's font size in a loop until it fit; scaling the laid-out
 * row achieves the same on narrow phones without re-measuring text repeatedly.
 */
@Composable
fun ScaleToFit(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
        val placeables = measurables.map { it.measure(loose) }
        val natural = placeables.maxOfOrNull { it.width } ?: 0
        val scale = if (natural > constraints.maxWidth && natural > 0) {
            constraints.maxWidth.toFloat() / natural
        } else {
            1f
        }
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else (natural * scale).roundToInt()
        val height = (placeables.maxOfOrNull { it.height } ?: 0).let { (it * scale).roundToInt() }
        layout(width, height) {
            placeables.forEach { p ->
                // Scaling happens about the horizontal centre, so centre the unscaled placeable.
                val x = ((width - p.width) / 2f).roundToInt()
                p.placeWithLayer(x, 0) {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
            }
        }
    }
}
