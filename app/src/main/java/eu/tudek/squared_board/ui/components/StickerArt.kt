package eu.tudek.squared_board.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.tudek.squared_board.data.StickerId
import eu.tudek.squared_board.ui.theme.Ink

/**
 * One stroke of a doodle, authored on the same 24x24 grid as the icons next door.
 *
 * A sticker is two to five of these stacked back to front and nothing else — keeping the art
 * a table rather than sixteen hand-written Canvas functions is the only reason the album can
 * grow by a line instead of a file.
 */
private data class Doodle(
    val path: String,
    val fill: Color? = null,
    val stroke: Color? = Ink.ink,
    val width: Float = 2.2f,
)

/** A circle as two arcs, which is all PathParser needs to see. */
private fun circle(cx: Double, cy: Double, r: Double) =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"

private val ART: Map<StickerId, List<Doodle>> = mapOf(
    StickerId.TABLE_1 to listOf( // rocket
        Doodle("M12 2.4C14.9 5.3 16.4 9 16.4 12.7V16H7.6v-3.3C7.6 9 9.1 5.3 12 2.4z", Ink.white),
        Doodle("M7.6 12.6L4.8 16.6v2.8l2.8-1.8zM16.4 12.6l2.8 4v2.8l-2.8-1.8z", Ink.red),
        Doodle(circle(12.0, 9.2, 2.1), Ink.blue, width = 1.8f),
        Doodle("M9.9 18.6c.4 2.2 1.1 3.4 2.1 3.4s1.7-1.2 2.1-3.4z", Ink.yellow, width = 1.8f),
    ),
    StickerId.TABLE_2 to listOf( // cat
        Doodle("M6.6 8.4L6 3.6l4.2 2.4zM17.4 8.4L18 3.6l-4.2 2.4z", Ink.white),
        Doodle(circle(12.0, 12.6, 6.2), Ink.white),
        Doodle(circle(9.8, 11.8, 0.9), Ink.ink, stroke = null),
        Doodle(circle(14.2, 11.8, 0.9), Ink.ink, stroke = null),
        Doodle("M4.2 13.4h2.6M4.4 15.6l2.6-.8M19.8 13.4h-2.6M19.6 15.6l-2.6-.8", width = 1.6f),
    ),
    StickerId.TABLE_3 to listOf( // ice cream
        Doodle("M7 12.6L12 22.4l5-9.8z", Ink.yellow),
        Doodle("M7.6 15.8l8.8-.1", width = 1.4f),
        Doodle(circle(12.0, 9.6, 3.9), Ink.red),
        Doodle(circle(12.0, 5.4, 3.2), Ink.green),
    ),
    StickerId.TABLE_4 to listOf( // dinosaur
        // Tail, legs and spines go down first so the body only has to cover their roots.
        Doodle("M5 14.6C2.8 14 1.8 12.2 2.2 10.2", width = 2.6f),
        Doodle("M7.4 17.6v3.6M12.4 17.6v3.6", width = 3.2f),
        Doodle("M7.6 11.4l1-1.9 1 1.9M11 11l1-1.9 1 1.9", width = 1.8f),
        Doodle("M13.4 12.6l3.2-4.4", width = 3.4f),
        Doodle("M4.8 14.6a5.2 4 0 1 0 10.4 0a5.2 4 0 1 0-10.4 0z", Ink.green),
        Doodle(circle(17.4, 6.8, 3.0), Ink.green),
        Doodle(circle(18.4, 6.2, 0.8), Ink.ink, stroke = null),
    ),
    StickerId.TABLE_5 to listOf( // butterfly
        Doodle("M11.3 12C8.6 8.6 3.4 6.2 2.6 9.1c-.8 2.8 2.3 5 5.5 5.4-2.8 1-4.3 3.6-2.9 5.4 1.4 1.7 5-.4 6.1-3.6z", Ink.blue),
        Doodle("M12.7 12c2.7-3.4 7.9-5.8 8.7-2.9.8 2.8-2.3 5-5.5 5.4 2.8 1 4.3 3.6 2.9 5.4-1.4 1.7-5-.4-6.1-3.6z", Ink.blue),
        Doodle(circle(7.4, 10.6, 1.1), Ink.yellow, stroke = null),
        Doodle(circle(16.6, 10.6, 1.1), Ink.yellow, stroke = null),
        Doodle("M12 7.4v9.8M12 7.6L9.7 4.5M12 7.6l2.3-3.1", width = 2.4f),
    ),
    StickerId.TABLE_6 to listOf( // robot
        Doodle("M4.4 11h-2v3.4h2M19.6 11h2v3.4h-2", width = 2f),
        Doodle("M6 7.4h12a1.6 1.6 0 0 1 1.6 1.6v8a1.6 1.6 0 0 1-1.6 1.6H6a1.6 1.6 0 0 1-1.6-1.6V9A1.6 1.6 0 0 1 6 7.4z", Ink.white),
        Doodle("M12 7.2V4.4", width = 2f),
        Doodle(circle(12.0, 3.2, 1.3), Ink.red, width = 1.8f),
        Doodle(circle(9.2, 11.6, 1.4), Ink.blue, width = 1.8f),
        Doodle(circle(14.8, 11.6, 1.4), Ink.blue, width = 1.8f),
        Doodle("M8.8 15.4h6.4", width = 2f),
    ),
    StickerId.TABLE_7 to listOf( // rainbow
        Doodle("M2.8 18.6a9.2 9.2 0 0 1 18.4 0", stroke = Ink.red, width = 3.2f),
        Doodle("M6.2 18.6a5.8 5.8 0 0 1 11.6 0", stroke = Ink.yellow, width = 3.2f),
        Doodle("M9.4 18.6a2.6 2.6 0 0 1 5.2 0", stroke = Ink.green, width = 3.2f),
        Doodle(circle(3.6, 19.6, 2.4), Ink.white, width = 1.8f),
        Doodle(circle(20.4, 19.6, 2.4), Ink.white, width = 1.8f),
    ),
    StickerId.TABLE_8 to listOf( // cupcake
        Doodle("M6.2 12.4h11.6l-1.3 8.2a1 1 0 0 1-1 .8H8.5a1 1 0 0 1-1-.8z", Ink.white),
        Doodle("M9.8 13.2l.8 7.4M12 13.2v7.4M14.2 13.2l-.8 7.4", width = 1.6f),
        Doodle("M6.3 12.4c-.7-2.1.8-3.8 2.5-3.7C9.2 6.4 10.5 5 12 5s2.8 1.4 3.2 3.7c1.7-.1 3.2 1.6 2.5 3.7z", Ink.red),
        Doodle(circle(12.0, 3.6, 1.4), Ink.red, width = 1.8f),
    ),
    StickerId.TABLE_9 to listOf( // dragon, seen head on so it cannot be mistaken for the dinosaur
        Doodle("M7.6 5.4L5.6 1.8l3.8 1.6zM16.4 5.4l2-3.6-3.8 1.6z", Ink.green),
        Doodle("M5.6 10.4a6.4 6.4 0 0 1 12.8 0v3.4a6.4 6.4 0 0 1-12.8 0z", Ink.green),
        Doodle(circle(9.5, 10.2, 1.7), Ink.white, width = 1.6f),
        Doodle(circle(14.5, 10.2, 1.7), Ink.white, width = 1.6f),
        Doodle(circle(9.9, 10.4, 0.7), Ink.ink, stroke = null),
        Doodle(circle(14.9, 10.4, 0.7), Ink.ink, stroke = null),
        Doodle("M10.8 14.8h.1M13.1 14.8h.1", width = 1.6f),
        Doodle("M9.4 17.2h5.2", width = 1.8f),
        Doodle("M10.4 17.3l.8 1.9.8-1.9zM12.8 17.3l.8 1.9.8-1.9z", Ink.white, width = 1.4f),
    ),
    StickerId.TABLE_10 to listOf( // crown
        Doodle("M3.4 18.2L4.6 7.4l4 3.6L12 4.8l3.4 6.2 4-3.6 1.2 10.8z", Ink.yellow),
        Doodle("M3.4 18.2h17.2v2.8H3.4z", Ink.yellow),
        Doodle(circle(7.8, 15.2, 1.0), Ink.red, stroke = null),
        Doodle(circle(12.0, 14.4, 1.0), Ink.blue, stroke = null),
        Doodle(circle(16.2, 15.2, 1.0), Ink.green, stroke = null),
    ),
    StickerId.STARS_10 to listOf( // sun
        Doodle("M12 1.4v3.2M12 19.4v3.2M1.4 12h3.2M19.4 12h3.2M4.5 4.5l2.2 2.2M17.3 17.3l2.2 2.2M19.5 4.5l-2.2 2.2M6.7 17.3l-2.2 2.2", width = 2.4f),
        Doodle(circle(12.0, 12.0, 5.2), Ink.yellow),
    ),
    StickerId.STARS_50 to listOf( // balloon
        Doodle("M12 2.2c3.8 0 6.6 3.1 6.6 6.9 0 4.2-4 7.6-6.6 9.4-2.6-1.8-6.6-5.2-6.6-9.4C5.4 5.3 8.2 2.2 12 2.2z", Ink.red),
        Doodle("M10.9 18.1h2.2L12 19.8z", Ink.red, width = 1.8f),
        Doodle("M12 19.9c1.5 1 .3 2.5-.8 3.2", width = 1.8f),
    ),
    StickerId.BOARD_FULL to listOf( // medal
        Doodle("M5.8 2.2h3.4l3.4 7.8H9.2z", Ink.blue),
        Doodle("M18.2 2.2h-3.4L11.4 10h3.4z", Ink.red),
        Doodle(circle(12.0, 15.4, 6.2), Ink.yellow),
        Doodle(circle(12.0, 15.4, 3.2), null, width = 1.8f),
    ),
    StickerId.RACE_20 to listOf( // train
        Doodle(circle(5.8, 5.8, 1.9), Ink.white, width = 1.8f),
        Doodle("M4.4 8.6h2.8v3.6H4.4z", Ink.blue),
        Doodle("M10.6 7.2h4.8v5h-4.8z", Ink.blue),
        Doodle("M2.6 12.2h13.4v5.8H2.6z", Ink.blue),
        Doodle(circle(6.2, 19.2, 2.1), Ink.white),
        Doodle(circle(13.0, 19.2, 2.1), Ink.white),
    ),
    StickerId.STREAK_3 to listOf( // flower
        Doodle("M12 11.4v10.4", stroke = Ink.green, width = 2.6f),
        Doodle("M12 17.2c-1.7-2.5-4.2-2.3-4.8-1.5.4 2.1 2.7 3.4 4.8 1.5z", Ink.green, width = 1.8f),
        Doodle(circle(12.0, 5.4, 2.6), Ink.red),
        Doodle(circle(8.6, 7.9, 2.6), Ink.red),
        Doodle(circle(9.9, 11.9, 2.6), Ink.red),
        Doodle(circle(14.1, 11.9, 2.6), Ink.red),
        Doodle(circle(15.4, 7.9, 2.6), Ink.red),
        Doodle(circle(12.0, 9.0, 2.4), Ink.yellow),
    ),
    StickerId.STREAK_7 to listOf( // cake
        Doodle("M8.4 6.6v4.2M12 6.6v4.2M15.6 6.6v4.2", width = 2f),
        Doodle(circle(8.4, 4.8, 1.1), Ink.yellow, width = 1.6f),
        Doodle(circle(12.0, 4.8, 1.1), Ink.yellow, width = 1.6f),
        Doodle(circle(15.6, 4.8, 1.1), Ink.yellow, width = 1.6f),
        Doodle("M5.8 10.8h12.4v4.8H5.8z", Ink.red),
        Doodle("M3.4 15.6h17.2v5.6H3.4z", Ink.white),
    ),
)

/**
 * An unlocked sticker is drawn in colour; a locked one is the same line work in pencil grey,
 * so a child can see the shape waiting for them without it looking finished.
 */
@Composable
fun StickerIcon(
    id: StickerId,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    val layers = ART[id] ?: return
    val paths = remember(id) { layers.map { PathParser().parsePathString(it.path).toPath() } }
    Canvas(modifier.size(size)) {
        layers.forEachIndexed { i, layer ->
            if (unlocked) {
                drawOn24Grid(paths[i], layer.stroke, layer.fill, layer.width)
            } else {
                drawOn24Grid(paths[i], Ink.grid, null, layer.width)
            }
        }
    }
}
