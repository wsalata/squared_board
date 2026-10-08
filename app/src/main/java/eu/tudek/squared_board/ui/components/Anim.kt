package eu.tudek.squared_board.ui.components

import androidx.compose.animation.core.CubicBezierEasing

/** The prototype's `cubic-bezier(.3, 1.6, .5, 1)` overshoot: things land with a small bounce. */
val PopEasing = CubicBezierEasing(0.3f, 1.6f, 0.5f, 1f)
