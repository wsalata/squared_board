package eu.tudek.squared_board.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import eu.tudek.squared_board.ui.theme.Ink

/** The squared-paper backdrop: 1dp rules every 22dp, exactly like the prototype's CSS gradients. */
fun Modifier.paperGrid(): Modifier = this.drawBehind {
    drawRect(Ink.paper)
    val step = 22.dp.toPx()
    val line = 1.dp.toPx()
    var x = 0f
    while (x <= size.width) {
        drawRect(Ink.grid, topLeft = Offset(x, 0f), size = Size(line, size.height))
        x += step
    }
    var y = 0f
    while (y <= size.height) {
        drawRect(Ink.grid, topLeft = Offset(0f, y), size = Size(size.width, line))
        y += step
    }
}
