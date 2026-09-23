package eu.tudek.squared_board.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * An inset outline, the equivalent of CSS `box-shadow: inset 0 0 0 Npx`.
 *
 * Unlike [Modifier.border] the line is drawn wholly inside the bounds, which is what keeps the
 * board's tiles flush against each other.
 */
fun Modifier.innerOutline(color: Color, width: Dp, corner: Dp): Modifier = drawWithContent {
    drawContent()
    val w = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(w / 2f, w / 2f),
        size = Size(size.width - w, size.height - w),
        cornerRadius = CornerRadius((corner.toPx() - w / 2f).coerceAtLeast(0f)),
        style = Stroke(width = w),
    )
}

/** A bare text link with a comfortable touch area — the prototype's `.text-btn`. */
fun Modifier.textButton(onClick: () -> Unit): Modifier =
    clickable(role = Role.Button, onClick = onClick).padding(horizontal = 4.dp, vertical = 6.dp)

/** A rounded box whose outline can be dashed, for the equation's answer slot. */
fun Modifier.dashedRoundBox(
    outline: Color,
    fill: Color,
    dashed: Boolean,
    width: Dp,
    corner: Dp,
): Modifier = drawBehind {
    val w = width.toPx()
    val radius = CornerRadius(corner.toPx())
    val inset = Offset(w / 2f, w / 2f)
    val inner = Size(size.width - w, size.height - w)
    if (fill != Color.Transparent) {
        drawRoundRect(fill, topLeft = inset, size = inner, cornerRadius = radius)
    }
    drawRoundRect(
        color = outline,
        topLeft = inset,
        size = inner,
        cornerRadius = radius,
        style = Stroke(
            width = w,
            pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(w * 2.4f, w * 1.8f)) else null,
        ),
    )
}
