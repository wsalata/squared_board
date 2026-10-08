package eu.tudek.squared_board.ui

import androidx.compose.animation.core.Animatable
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
import eu.tudek.squared_board.data.StickerId
import eu.tudek.squared_board.game.ResultState
import eu.tudek.squared_board.game.Stickers
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import eu.tudek.squared_board.R
import eu.tudek.squared_board.game.resolve
import eu.tudek.squared_board.ui.components.PopEasing
import eu.tudek.squared_board.ui.components.SketchSurface
import eu.tudek.squared_board.ui.components.SketchButton
import eu.tudek.squared_board.ui.components.StarIcon
import eu.tudek.squared_board.ui.components.StickerIcon
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
                    stringResource(state.title),
                    style = AppType.h1.copy(fontSize = 34.sp, lineHeight = 36.sp),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    state.summary.resolve(),
                    style = AppType.body.copy(fontSize = 20.sp, lineHeight = 26.sp, color = Ink.inkSoft),
                    textAlign = TextAlign.Center,
                )
                if (state.bonusStar) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.daily_bonus_star),
                        style = AppType.bodyBold.copy(fontSize = 17.sp, color = Ink.green),
                        textAlign = TextAlign.Center,
                    )
                }
                if (state.streak >= 2) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        pluralStringResource(R.plurals.streak_days, state.streak, state.streak),
                        style = AppType.body.copy(color = Ink.inkSoft),
                        textAlign = TextAlign.Center,
                    )
                }
                if (state.newStickers.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    StickerReveal(state.newStickers, animationsOn)
                }
                if (state.wrong.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text(stringResource(R.string.result_review), style = AppType.h2)
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
            ResultAction(stringResource(R.string.result_play_again), Ink.green, Ink.white, onAgain)
            ResultAction(stringResource(R.string.result_back_to_menu), Ink.white, Ink.ink, onMenu)
        }
    }
}

/**
 * The payoff: whatever the round just unlocked, popping in one after another on the same
 * overshoot curve as the stars above it.
 */
@Composable
private fun StickerReveal(ids: List<StickerId>, animationsOn: Boolean) {
    val shown = ids.take(3)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            pluralStringResource(R.plurals.sticker_new, ids.size, ids.size),
            style = AppType.h2.copy(fontSize = 20.sp),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            shown.forEachIndexed { k, id ->
                val pop = remember(id) { Animatable(if (animationsOn) 0f else 1f) }
                LaunchedEffect(id, animationsOn) {
                    if (animationsOn) {
                        pop.snapTo(0f)
                        pop.animateTo(1f, tween(durationMillis = 450, delayMillis = 200 + k * 180, easing = PopEasing))
                    }
                }
                SketchSurface(
                    modifier = Modifier.graphicsLayer {
                        val v = pop.value
                        scaleX = v
                        scaleY = v
                        alpha = if (v <= 0f) 0f else 1f
                    },
                    background = Ink.greenSoft,
                    border = Ink.green,
                    borderWidth = 2.dp,
                    cornerRadius = 14.dp,
                    depth = 0.dp,
                ) {
                    Column(
                        Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        StickerIcon(id, unlocked = true, size = 60.dp)
                        Text(
                            stringResource(Stickers[id].name),
                            style = AppType.body.copy(fontSize = 13.sp, color = Ink.ink),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        if (ids.size > shown.size) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.sticker_new_more, ids.size - shown.size),
                style = AppType.body.copy(color = Ink.inkSoft),
            )
        }
    }
}

/** Three big stars that pop in one after another, the middle one riding higher. */
@Composable
private fun BigStars(stars: Int, animationsOn: Boolean) {
    val starsLabel = pluralStringResource(R.plurals.result_stars_label, stars, stars)
    Row(
        Modifier
            .padding(top = 4.dp, bottom = 10.dp)
            .semantics { contentDescription = starsLabel },
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
