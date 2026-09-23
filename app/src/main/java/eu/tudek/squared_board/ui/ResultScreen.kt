package eu.tudek.squared_board.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tudek.squared_board.game.ResultState
import eu.tudek.squared_board.game.plural
import eu.tudek.squared_board.ui.components.SketchSurface
import eu.tudek.squared_board.ui.components.SketchButton
import eu.tudek.squared_board.ui.components.StarIcon
import eu.tudek.squared_board.ui.theme.AppType
import eu.tudek.squared_board.ui.theme.Ink

@Composable
fun ResultScreen(
    state: ResultState,
    animationsOn: Boolean,
    onAgain: () -> Unit,
    onMenu: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SketchSurface(Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BigStars(state.stars, animationsOn)
                Text(
                    state.title,
                    style = AppType.h1.copy(fontSize = 34.sp, lineHeight = 36.sp),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    state.summary,
                    style = AppType.body.copy(fontSize = 20.sp, lineHeight = 26.sp, color = Ink.inkSoft),
                    textAlign = TextAlign.Center,
                )
                if (state.wrong.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text("Do powtórki", style = AppType.h2)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.wrong.forEach { fact ->
                            SketchSurface(
                                background = Ink.redSoft,
                                border = Ink.red,
                                borderWidth = 2.dp,
                                cornerRadius = 10.dp,
                                depth = 0.dp,
                            ) {
                                Text(
                                    fact,
                                    style = AppType.h2.copy(fontSize = 19.sp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ResultAction("Zagraj jeszcze raz", Ink.green, Ink.white, onAgain)
            ResultAction("Wróć do menu", Ink.white, Ink.ink, onMenu)
        }
    }
}

/** Three big stars that pop in one after another, the middle one riding higher. */
@Composable
private fun BigStars(stars: Int, animationsOn: Boolean) {
    Row(
        Modifier
            .padding(top = 4.dp, bottom = 10.dp)
            .semantics { contentDescription = "$stars ${plural(stars, "gwiazdka", "gwiazdki", "gwiazdek")} na 3" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (k in 1..3) {
            val pop = remember(k) { Animatable(if (animationsOn) 0f else 1f) }
            LaunchedEffect(stars, animationsOn) {
                if (animationsOn) {
                    pop.snapTo(0f)
                    pop.animateTo(
                        1f,
                        tween(durationMillis = 450, delayMillis = (k * 180), easing = PopEasing),
                    )
                }
            }
            StarIcon(
                filled = k <= stars,
                size = 78.dp,
                modifier = Modifier
                    .offset(y = if (k == 2) (-14).dp else 0.dp)
                    .graphicsLayer {
                        val p = pop.value
                        scaleX = p
                        scaleY = p
                        alpha = if (p <= 0f) 0f else 1f
                        rotationZ = -30f * (1f - p)
                    },
            )
        }
    }
}

/** The prototype's `cubic-bezier(.3, 1.6, .5, 1)` overshoot. */
private val PopEasing = CubicBezierEasing(0.3f, 1.6f, 0.5f, 1f)

@Composable
private fun ResultAction(
    label: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    SketchButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), background = background) {
        Box(Modifier.height(60.dp), contentAlignment = Alignment.Center) {
            Text(label, style = AppType.button.copy(fontSize = 21.sp, color = textColor))
        }
    }
}
