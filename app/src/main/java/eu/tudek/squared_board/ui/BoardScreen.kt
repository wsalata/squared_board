package eu.tudek.squared_board.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.ui.components.BackIcon
import eu.tudek.squared_board.ui.components.SketchButton
import eu.tudek.squared_board.ui.components.SketchSurface
import eu.tudek.squared_board.ui.components.innerOutline
import eu.tudek.squared_board.ui.theme.AppType
import eu.tudek.squared_board.ui.theme.Ink
import kotlin.math.max
import kotlin.math.min

@Composable
fun BoardScreen(
    progress: Progress,
    selected: Pair<Int, Int>?,
    resetArmed: Boolean,
    onBack: () -> Unit,
    onSelect: (Int, Int) -> Unit,
    onReset: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SketchButton(
                onClick = onBack,
                cornerRadius = 22.dp,
                modifier = Modifier.size(width = 44.dp, height = 48.dp),
                contentDescription = "Wróć do menu",
            ) { BackIcon() }
            Text("Moja tabliczka", style = AppType.h1.copy(fontSize = 28.sp))
        }

        Legend()
        BoardGrid(progress, selected, onSelect)
        TileCaption(progress, selected)

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SketchButton(onClick = onReset, background = if (resetArmed) Ink.red else Ink.white) {
                Box(
                    Modifier.height(46.dp).padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (resetArmed) "Na pewno? Dotknij jeszcze raz" else "Wyzeruj postępy",
                        style = AppType.label.copy(color = if (resetArmed) Ink.white else Ink.ink),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun Legend() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LegendItem("Nowe", Ink.tiers[0], outlined = true)
        LegendItem("Ćwiczę", Ink.tiers[2], outlined = false)
        LegendItem("Umiem", Ink.tiers[5], outlined = false)
    }
}

@Composable
private fun LegendItem(label: String, color: Color, outlined: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color)
                .then(if (outlined) Modifier.innerOutline(Ink.grid, 1.5.dp, 5.dp) else Modifier),
        )
        Text(label, style = AppType.body.copy(color = Ink.inkSoft))
    }
}

/**
 * The full 11x11 board: a header row and column of factors, then one tile per fact,
 * shaded by how well the player knows it. Text is sized from the tile width so the
 * numbers stay inside their squares on narrow phones.
 */
@Composable
private fun BoardGrid(progress: Progress, selected: Pair<Int, Int>?, onSelect: (Int, Int) -> Unit) {
    SketchSurface(Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(10.dp)) {
            val tile = (maxWidth.value - 10 * 3f) / 11f
            val tileFont = min(max(10f, tile * 0.42f), 15f).sp
            val headerFont = min(max(12f, tile * 0.48f), 17f).sp
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Header("·", on = false, font = headerFont, modifier = Modifier.weight(1f))
                    for (c in 1..10) {
                        Header("$c", on = selected?.second == c, font = headerFont, modifier = Modifier.weight(1f))
                    }
                }
                for (r in 1..10) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Header("$r", on = selected?.first == r, font = headerFont, modifier = Modifier.weight(1f))
                        for (c in 1..10) {
                            Tile(
                                level = progress.score(r, c),
                                value = r * c,
                                selected = selected == r to c,
                                font = tileFont,
                                modifier = Modifier.weight(1f),
                                label = "$r razy $c równa się ${r * c}",
                                onClick = { onSelect(r, c) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(text: String, on: Boolean, font: TextUnit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(if (on) Ink.ink else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = AppType.h2.copy(fontSize = font, color = if (on) Ink.white else Ink.inkSoft),
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Tile(
    level: Int,
    value: Int,
    selected: Boolean,
    font: TextUnit,
    modifier: Modifier,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .aspectRatio(1f)
            .scale(if (selected) 0.92f else 1f)
            .clip(RoundedCornerShape(6.dp))
            .background(Ink.tiers[level])
            .then(
                if (selected) Modifier.innerOutline(Ink.ink, 3.dp, 6.dp) else Modifier.innerOutline(Ink.grid, 1.5.dp, 6.dp),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$value",
            style = AppType.bodyBold.copy(
                fontSize = font,
                color = if (selected) Ink.ink else Ink.onTier(level),
            ),
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Composable
private fun TileCaption(progress: Progress, selected: Pair<Int, Int>?) {
    Box(Modifier.fillMaxWidth().heightIn(min = 54.dp), contentAlignment = Alignment.TopCenter) {
        if (selected == null) {
            Text(
                "Dotknij kratkę, żeby zobaczyć działanie.",
                style = AppType.h2.copy(fontSize = 19.sp, lineHeight = 25.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        } else {
            val (r, c) = selected
            val level = progress.score(r, c)
            Column(Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$r · $c = ${r * c}, ${r * c} : $c = $r",
                    style = AppType.h2.copy(fontSize = 19.sp, lineHeight = 25.sp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    when {
                        level >= 4 -> "Umiesz to świetnie!"
                        level >= 1 -> "Już to ćwiczysz. Jeszcze trochę!"
                        else -> "Tego działania jeszcze nie było w grze."
                    },
                    style = AppType.body.copy(color = Ink.inkSoft),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
