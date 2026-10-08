package eu.tudek.squared_board.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tudek.squared_board.game.DailyState
import eu.tudek.squared_board.ui.theme.AppType
import eu.tudek.squared_board.ui.theme.Ink
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * The last seven days, ticked where the child practised. Today's box carries the heavier
 * outline the board uses for a selected tile, so "did I do it yet?" is answerable at a glance.
 *
 * Decorative to a screen reader, like the mini board on the home screen: the streak is
 * spelled out in words right beside it, and seven separate day labels inside one card would
 * be read out as noise.
 */
@Composable
fun StreakStrip(daily: DailyState, animationsOn: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        daily.days.forEachIndexed { i, played ->
            DayBox(
                day = daily.firstDay + i,
                played = played,
                today = i == daily.days.lastIndex,
                index = i,
                animationsOn = animationsOn,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayBox(
    day: Int,
    played: Boolean,
    today: Boolean,
    index: Int,
    animationsOn: Boolean,
    modifier: Modifier,
) {
    // Derived from the date rather than translated: the weekday names Android already has
    // follow the device language, and cannot drift between the two string files.
    val letter = remember(day) {
        LocalDate.ofEpochDay(day.toLong()).dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            letter,
            style = AppType.body.copy(fontSize = 12.sp, lineHeight = 14.sp, color = Ink.inkSoft),
        )
        Spacer(Modifier.height(3.dp))
        val pop = remember(index) { Animatable(if (animationsOn && played) 0f else 1f) }
        LaunchedEffect(played, animationsOn) {
            if (animationsOn && played) {
                pop.snapTo(0f)
                pop.animateTo(1f, tween(durationMillis = 320, delayMillis = index * 60, easing = PopEasing))
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (played) Ink.green else Ink.white)
                .then(
                    if (today) {
                        Modifier.innerOutline(Ink.ink, 3.dp, 7.dp)
                    } else {
                        Modifier.innerOutline(Ink.grid, 1.5.dp, 7.dp)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (played) {
                TickIcon(
                    size = 16.dp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = pop.value
                        scaleY = pop.value
                    },
                )
            }
        }
    }
}
