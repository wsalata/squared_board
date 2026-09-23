package eu.tudek.squared_board.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import eu.tudek.squared_board.data.OpMode
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.game.Mode
import eu.tudek.squared_board.ui.components.PlayIcon
import eu.tudek.squared_board.ui.components.SketchButton
import eu.tudek.squared_board.ui.components.SketchSurface
import eu.tudek.squared_board.ui.components.SoundIcon
import eu.tudek.squared_board.ui.components.dashedRoundBox
import eu.tudek.squared_board.ui.components.innerOutline
import eu.tudek.squared_board.ui.components.textButton
import eu.tudek.squared_board.ui.components.StarIcon
import eu.tudek.squared_board.ui.components.StopwatchIcon
import eu.tudek.squared_board.ui.theme.AppType
import eu.tudek.squared_board.ui.theme.Ink

@Composable
fun HomeScreen(
    progress: Progress,
    onToggleSound: () -> Unit,
    onOpenBoard: () -> Unit,
    onOp: (OpMode) -> Unit,
    onToggleTable: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onInput: (InputMode) -> Unit,
    onPlay: (Mode) -> Unit,
) {
    val settings = progress.settings
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HomeHeader(progress.stars, settings.sound, onToggleSound)
        ProgressCard(progress, onOpenBoard)

        Section(stringResource(R.string.section_practice)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OpChoice("3 · 4", stringResource(R.string.op_multiplication), settings.op == OpMode.MUL, Modifier.weight(1f)) { onOp(OpMode.MUL) }
                    OpChoice("12 : 4", stringResource(R.string.op_division), settings.op == OpMode.DIV, Modifier.weight(1f)) { onOp(OpMode.DIV) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OpChoice("3 · 4   12 : 4", stringResource(R.string.op_mixed), settings.op == OpMode.MIX, Modifier.weight(1f)) { onOp(OpMode.MIX) }
                    OpChoice(
                        example = "· 4 = 12",
                        label = stringResource(R.string.op_puzzles),
                        selected = settings.op == OpMode.MISS,
                        modifier = Modifier.weight(1f),
                        leadingBlank = true,
                    ) { onOp(OpMode.MISS) }
                }
            }
        }

        Section(
            title = stringResource(R.string.section_tables),
            action = if (settings.tables.size < 10) stringResource(R.string.tables_select_all) to onSelectAll else null,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (rowStart in listOf(1, 6)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (n in rowStart until rowStart + 5) {
                            val on = n in settings.tables
                            SketchButton(
                                onClick = { onToggleTable(n) },
                                modifier = Modifier.weight(1f),
                                background = if (on) Ink.blue else Ink.white,
                                cornerRadius = 12.dp,
                                contentDescription = stringResource(R.string.tables_table_of, n),
                            ) {
                                Box(Modifier.height(46.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        "$n",
                                        style = AppType.h2.copy(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (on) Ink.white else Ink.ink,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Section(stringResource(R.string.section_input)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SegmentButton(stringResource(R.string.input_choice), settings.input == InputMode.CHOICE, Modifier.weight(1f)) {
                    onInput(InputMode.CHOICE)
                }
                SegmentButton(stringResource(R.string.input_keypad), settings.input == InputMode.KEYPAD, Modifier.weight(1f)) {
                    onInput(InputMode.KEYPAD)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
            PlayButton(
                title = stringResource(R.string.play_adventure),
                detail = stringResource(R.string.play_adventure_detail),
                background = Ink.green,
                titleColor = Ink.white,
                detailColor = Color(0xFFE7FFF3),
                onClick = { onPlay(Mode.ADVENTURE) },
            ) { PlayIcon() }
            PlayButton(
                title = stringResource(R.string.play_race),
                detail = if (progress.bestRace > 0) {
                    stringResource(R.string.play_race_detail_record, progress.bestRace)
                } else {
                    stringResource(R.string.play_race_detail)
                },
                background = Ink.white,
                titleColor = Ink.ink,
                detailColor = Ink.inkSoft,
                onClick = { onPlay(Mode.RACE) },
            ) { StopwatchIcon() }
        }
    }
}

@Composable
private fun HomeHeader(stars: Int, soundOn: Boolean, onToggleSound: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        val starsLabel = stringResource(R.string.home_stars_collected, stars)
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.home_title), style = AppType.h1)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.home_subtitle), style = AppType.body.copy(color = Ink.inkSoft))
        }
        SketchSurface(
            background = Ink.white,
            cornerRadius = 22.dp,
            modifier = Modifier.height(48.dp).semantics { contentDescription = starsLabel },
        ) {
            Row(
                Modifier.padding(start = 8.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StarIcon(filled = true)
                Text("$stars", style = AppType.h2.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold))
            }
        }
        SketchButton(
            onClick = onToggleSound,
            cornerRadius = 22.dp,
            modifier = Modifier.size(width = 44.dp, height = 48.dp),
            contentDescription = stringResource(if (soundOn) R.string.home_sound_off else R.string.home_sound_on),
        ) {
            SoundIcon(on = soundOn)
        }
    }
}

/** The tappable summary card: a 10x10 thumbnail of the board beside the mastered count. */
@Composable
private fun ProgressCard(progress: Progress, onOpenBoard: () -> Unit) {
    val mastered = progress.mastered()
    SketchButton(
        onClick = onOpenBoard,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        contentDescription = stringResource(R.string.progress_open_board),
    ) {
        Row(
            Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiniBoard(progress)
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$mastered", style = AppType.h1.copy(fontSize = 36.sp, lineHeight = 36.sp))
                    Text(
                        " / 100",
                        style = AppType.h2.copy(fontSize = 18.sp, color = Ink.inkSoft),
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    when {
                        mastered == 0 -> stringResource(R.string.progress_none)
                        mastered == 100 -> stringResource(R.string.progress_all)
                        else -> pluralStringResource(R.plurals.progress_some, mastered, mastered)
                    },
                    style = AppType.body.copy(color = Ink.inkSoft),
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.progress_my_board), style = AppType.bodyBold.copy(color = Ink.blue))
            }
        }
    }
}

@Composable
private fun MiniBoard(progress: Progress) {
    val cell = 13.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.clearAndSetSemantics { },
    ) {
        for (r in 1..10) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (c in 1..10) {
                    val level = progress.score(r, c)
                    Box(
                        Modifier
                            .size(cell)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Ink.tiers[level])
                            .then(
                                // Untouched cells keep the faint pencil outline of the paper grid.
                                if (level == 0) {
                                    Modifier.innerOutline(Ink.grid, 1.5.dp, 3.dp)
                                } else {
                                    Modifier
                                }
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    action: Pair<String, () -> Unit>? = null,
    content: @Composable () -> Unit,
) {
    Column {
        Row(
            Modifier.fillMaxWidth().height(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = AppType.h2)
            if (action != null) {
                Text(
                    action.first,
                    style = AppType.bodyBold.copy(color = Ink.blue),
                    modifier = Modifier.textButton(action.second),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun OpChoice(
    example: String,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    leadingBlank: Boolean = false,
    onClick: () -> Unit,
) {
    SketchButton(
        onClick = onClick,
        modifier = modifier,
        background = if (selected) Ink.yellow else Ink.white,
        contentDescription = if (leadingBlank) "$label: ile · 4 = 12" else "$label: $example",
    ) {
        Column(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp, start = 6.dp, end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val exampleStyle = AppType.h1.copy(fontSize = 22.sp, lineHeight = 24.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (leadingBlank) {
                    Text(
                        "?",
                        style = exampleStyle.copy(fontSize = 18.sp),
                        modifier = Modifier
                            .dashedRoundBox(Ink.ink, Color.Transparent, dashed = true, width = 2.dp, corner = 6.dp)
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
                Text(example, style = exampleStyle, maxLines = 1, textAlign = TextAlign.Center)
            }
            Text(
                label,
                style = AppType.body.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Ink.ink else Ink.inkSoft,
                ),
            )
        }
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    SketchButton(onClick = onClick, modifier = modifier, background = if (selected) Ink.ink else Ink.white) {
        Box(Modifier.height(46.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = AppType.label.copy(color = if (selected) Ink.white else Ink.ink),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PlayButton(
    title: String,
    detail: String,
    background: Color,
    titleColor: Color,
    detailColor: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    SketchButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), background = background) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = AppType.button.copy(color = titleColor))
                Text(
                    detail,
                    style = AppType.body.copy(
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = detailColor,
                    ),
                )
            }
            Spacer(Modifier.width(0.dp))
            icon()
        }
    }
}
