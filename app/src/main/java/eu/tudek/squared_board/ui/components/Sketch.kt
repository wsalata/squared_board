package eu.tudek.squared_board.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.tudek.squared_board.ui.theme.Ink

/**
 * The prototype's signature surface: a thick ink outline over a hard offset shadow.
 *
 * Pressing it drops the face onto the shadow — the face slides down by [depth] - 1dp while the
 * shadow shrinks to 1dp, so its bottom edge never moves and the layout stays still.
 */
@Composable
fun SketchSurface(
    modifier: Modifier = Modifier,
    background: Color = Ink.white,
    border: Color = Ink.ink,
    borderWidth: Dp = 3.dp,
    cornerRadius: Dp = 14.dp,
    depth: Dp = 4.dp,
    pressed: Boolean = false,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val drop = if (pressed) depth - 1.dp else 0.dp
    val shadowGap = depth - drop
    // One Box, not two: padding reserves the shadow strip while everything after it — the
    // offset, the shadow, the face — works on the remaining area, so the face still fills a
    // sized parent instead of shrinking to its content.
    Box(
        modifier
            .padding(bottom = depth)
            .offset(y = drop)
            .drawBehind {
                drawRoundRect(
                    color = border,
                    topLeft = Offset(0f, shadowGap.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                )
            }
            .background(background, shape)
            .border(borderWidth, border, shape)
            .clip(shape),
        contentAlignment = contentAlignment,
        content = content,
    )
}

/** [SketchSurface] wired to a click, tracking its own press state. */
@Composable
fun SketchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    background: Color = Ink.white,
    border: Color = Ink.ink,
    borderWidth: Dp = 3.dp,
    cornerRadius: Dp = 14.dp,
    depth: Dp = 4.dp,
    contentDescription: String? = null,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    SketchSurface(
        modifier = modifier.then(
            Modifier.clickableSurface(
                onClick = onClick,
                enabled = enabled,
                interactions = interactions,
                label = contentDescription,
            ),
        ),
        background = background,
        border = border,
        borderWidth = borderWidth,
        cornerRadius = cornerRadius,
        depth = depth,
        pressed = enabled && pressed,
        contentAlignment = contentAlignment,
        content = content,
    )
}

/**
 * Clicks without a ripple — the surface already answers the touch by dropping onto its shadow,
 * which is what the prototype did with `-webkit-tap-highlight-color: transparent`.
 */
private fun Modifier.clickableSurface(
    onClick: () -> Unit,
    enabled: Boolean,
    interactions: MutableInteractionSource,
    label: String?,
): Modifier = this
    .clickable(
        interactionSource = interactions,
        indication = null,
        enabled = enabled,
        role = Role.Button,
        onClick = onClick,
    )
    .then(if (label == null) Modifier else Modifier.semantics { contentDescription = label })
