package eu.tudek.squared_board.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import eu.tudek.squared_board.R
import eu.tudek.squared_board.data.InputMode
import eu.tudek.squared_board.game.GameState
import eu.tudek.squared_board.game.Mode
import eu.tudek.squared_board.game.RACE_MS
import eu.tudek.squared_board.game.ROUND
import eu.tudek.squared_board.game.SpokenWords
import eu.tudek.squared_board.game.Token
import eu.tudek.squared_board.game.resolve
import eu.tudek.squared_board.ui.components.BackspaceIcon
import eu.tudek.squared_board.ui.components.CloseIcon
import eu.tudek.squared_board.ui.components.ScaleToFit
import eu.tudek.squared_board.ui.components.SketchButton
import eu.tudek.squared_board.ui.components.SketchSurface
import eu.tudek.squared_board.ui.components.dashedRoundBox
import eu.tudek.squared_board.ui.components.innerOutline
import eu.tudek.squared_board.ui.theme.AppType
import eu.tudek.squared_board.ui.theme.Ink
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@Composable
fun GameScreen(
    state: GameState,
    input: InputMode,
    animationsOn: Boolean,
    onQuit: () -> Unit,
    onAnswer: (Int) -> Unit,
    onKey: (String) -> Unit,
    onNext: () -> Unit,
) {
    val race = state.mode == Mode.RACE
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxHeight()) {
        TopBar(state, race, onQuit)
        QuestionCard(state, animationsOn)
        ScoreLine(state, race)
        if (input == InputMode.CHOICE) {
            AnswerGrid(state, onAnswer)
        } else {
            Keypad(onKey)
        }
        if (!race) {
            // The button keeps its space even when hidden, so the keypad never jumps.
            SketchButton(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().alpha(if (state.showNext) 1f else 0f),
                enabled = state.showNext,
                background = Ink.yellow,
            ) {
                Box(Modifier.height(60.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(state.nextLabel), style = AppType.button)
                }
            }
        }
    }
}

@Composable
private fun TopBar(state: GameState, race: Boolean, onQuit: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SketchButton(
            onClick = onQuit,
            cornerRadius = 22.dp,
            modifier = Modifier.size(width = 44.dp, height = 48.dp),
            contentDescription = stringResource(R.string.game_quit),
        ) { CloseIcon() }

        if (race) TimerBar(state.millisLeft, Modifier.weight(1f)) else Track(state, Modifier.weight(1f))

        Text(
            if (state.streak >= 2) "🔥 ${state.streak}" else "",
            style = AppType.h2.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold),
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(56.dp),
        )
    }
}

/** Ten pips, one per question: outlined ahead, ink-ringed for the current one, then green or red. */
@Composable
private fun Track(state: GameState, modifier: Modifier = Modifier) {
    Row(
        modifier.clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (k in 0 until ROUND) {
            val result = state.results.getOrNull(k)
            val shape = RoundedCornerShape(4.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(16.dp)
                    .clip(shape)
                    .background(
                        when (result) {
                            true -> Ink.green
                            false -> Ink.red
                            null -> Ink.white
                        },
                    )
                    .then(
                        when {
                            result != null -> Modifier
                            k == state.index -> Modifier.innerOutline(Ink.ink, 2.5.dp, 4.dp)
                            else -> Modifier.innerOutline(Ink.grid, 2.dp, 4.dp)
                        },
                    ),
            )
        }
    }
}

/** The race clock, draining left to right and turning red in the last ten seconds. */
@Composable
private fun TimerBar(millisLeft: Long, modifier: Modifier = Modifier) {
    val fraction = (millisLeft.toFloat() / RACE_MS).coerceIn(0f, 1f)
    val low = millisLeft < 10_000
    Box(
        modifier
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Ink.white)
            .innerOutline(Ink.ink, 3.dp, 9.dp)
            .clearAndSetSemantics { },
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .padding(3.dp)
                .background(if (low) Ink.red else Ink.yellow, RoundedCornerShape(6.dp)),
        )
    }
}

@Composable
private fun QuestionCard(state: GameState, animationsOn: Boolean) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state.shakeTick) {
        if (state.shakeTick > 0 && animationsOn) {
            shake.snapTo(0f)
            shake.animateTo(1f, tween(durationMillis = 400))
        }
    }
    SketchSurface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 190.dp, max = 420.dp)
            .graphicsLayer { translationX = shakeOffset(shake.value) * density },
        cornerRadius = 18.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 18.dp, start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Equation(state)
            Text(
                state.feedback?.resolve().orEmpty(),
                style = AppType.h2.copy(
                    fontSize = 19.sp,
                    lineHeight = 24.sp,
                    color = when (state.feedbackOk) {
                        true -> Ink.green
                        false -> Ink.red
                        null -> Ink.ink
                    },
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.heightIn(min = 28.dp),
            )
        }
    }
}

/** Reproduces the prototype's four-step shake keyframes in dp. */
private fun shakeOffset(t: Float): Float = when {
    t <= 0f || t >= 1f -> 0f
    t < 0.2f -> -10f * (t / 0.2f)
    t < 0.4f -> -10f + 19f * ((t - 0.2f) / 0.2f)
    t < 0.6f -> 9f - 15f * ((t - 0.4f) / 0.2f)
    t < 0.8f -> -6f + 10f * ((t - 0.6f) / 0.2f)
    else -> 4f * (1f - (t - 0.8f) / 0.2f)
}

@Composable
private fun Equation(state: GameState) {
    val q = state.question
    val spoken = q.spoken(
        SpokenWords(
            times = stringResource(R.string.spoken_times),
            dividedBy = stringResource(R.string.spoken_divided_by),
            equals = stringResource(R.string.spoken_equals),
            blank = stringResource(R.string.spoken_blank),
        ),
    )
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // clamp(44px, 15vw, 72px) from the prototype's stylesheet; ScaleToFit takes over from there.
        val fontSize = min(max(44f, maxWidth.value * 0.15f), 72f)
        ScaleToFit(
            Modifier
                .fillMaxWidth()
                .semantics { contentDescription = spoken },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((fontSize * 0.22f).dp),
            ) {
                q.tokens.forEach { token ->
                    when (token) {
                        is Token.Num -> Text(
                            "${token.value}",
                            style = AppType.h1.copy(fontSize = fontSize.sp, lineHeight = (fontSize * 1.05f).sp),
                            maxLines = 1,
                        )
                        is Token.Sym -> Text(
                            token.text,
                            style = AppType.h1.copy(fontSize = fontSize.sp, lineHeight = (fontSize * 1.05f).sp),
                            maxLines = 1,
                        )
                        Token.Blank -> Blank(state, fontSize)
                    }
                }
            }
        }
    }
}

/** The answer slot: dashed while empty, solid once typed, green once the answer is revealed. */
@Composable
private fun Blank(state: GameState, fontSize: Float) {
    val filled = state.typed.isNotEmpty()
    // Once revealed the blank holds the correct answer, so it goes green either way — being
    // wrong is called out in the feedback line, not by marking the right number red.
    val outline = when {
        state.locked -> Ink.green
        filled -> Ink.ink
        else -> Ink.inkSoft
    }
    val fill = if (state.locked) Ink.greenSoft else Color.Transparent
    val dashed = !state.locked && !filled
    Box(
        Modifier
            .heightIn(min = (fontSize * 1.12f).dp)
            .dashedRoundBox(outline, fill, dashed, 4.dp, 14.dp)
            .padding(horizontal = (fontSize * 0.14f).dp, vertical = (fontSize * 0.04f).dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            state.blankText,
            style = AppType.h1.copy(
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.05f).sp,
                color = outline,
            ),
            maxLines = 1,
            modifier = Modifier.widthIn(min = (fontSize * 1.5f).dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ScoreLine(state: GameState, race: Boolean) {
    val style = AppType.label.copy(color = Ink.inkSoft)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (race) {
            Text(stringResource(R.string.game_race_correct, state.correct), style = style)
            Text("${ceil(state.millisLeft / 1000.0).toInt()} s", style = style)
        } else {
            Text(stringResource(R.string.game_question_of, state.shown, ROUND), style = style)
            Text(
                pluralStringResource(R.plurals.game_correct_count, state.correct, state.correct),
                style = style,
            )
        }
    }
}

@Composable
private fun AnswerGrid(state: GameState, onAnswer: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.choices.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { value ->
                    val right = state.locked && value == state.question.ans
                    val wrong = state.locked && value == state.chosen && value != state.question.ans
                    SketchButton(
                        onClick = { onAnswer(value) },
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (state.locked && !right && !wrong) 0.45f else 1f),
                        enabled = !state.locked,
                        background = when {
                            right -> Ink.green
                            wrong -> Ink.red
                            else -> Ink.white
                        },
                        cornerRadius = 18.dp,
                    ) {
                        Box(Modifier.height(84.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "$value",
                                style = AppType.h1.copy(
                                    fontSize = 38.sp,
                                    lineHeight = 40.sp,
                                    color = if (right || wrong) Ink.white else Ink.ink,
                                ),
                            )
                        }
                    }
                }
                // Keeps a lone last option from stretching across the row.
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Keypad(onKey: (String) -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("del", "0", "ok"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    SketchButton(
                        onClick = { onKey(key) },
                        modifier = Modifier.weight(1f),
                        background = if (key == "ok") Ink.green else Ink.white,
                        contentDescription = when (key) {
                            "del" -> stringResource(R.string.game_key_delete)
                            "ok" -> stringResource(R.string.game_key_submit)
                            else -> null
                        },
                    ) {
                        Box(Modifier.height(62.dp), contentAlignment = Alignment.Center) {
                            when (key) {
                                "del" -> BackspaceIcon()
                                "ok" -> Text("OK", style = AppType.digits.copy(fontSize = 24.sp, color = Ink.white))
                                else -> Text(key, style = AppType.digits)
                            }
                        }
                    }
                }
            }
        }
    }
}
