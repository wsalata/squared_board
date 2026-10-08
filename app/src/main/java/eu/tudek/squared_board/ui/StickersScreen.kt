package eu.tudek.squared_board.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tudek.squared_board.R
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.StickerId
import eu.tudek.squared_board.game.Sticker
import eu.tudek.squared_board.game.Stickers
import eu.tudek.squared_board.game.resolve
import eu.tudek.squared_board.ui.components.BackIcon
import eu.tudek.squared_board.ui.components.SketchButton
import eu.tudek.squared_board.ui.components.SketchSurface
import eu.tudek.squared_board.ui.components.StickerIcon
import eu.tudek.squared_board.ui.components.dashedRoundBox
import eu.tudek.squared_board.ui.components.innerOutline
import eu.tudek.squared_board.ui.theme.AppType
import eu.tudek.squared_board.ui.theme.Ink

private const val COLUMNS = 4

@Composable
fun StickersScreen(
    progress: Progress,
    selected: StickerId?,
    onBack: () -> Unit,
    onSelect: (StickerId) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SketchButton(
                onClick = onBack,
                cornerRadius = 22.dp,
                modifier = Modifier.size(width = 44.dp, height = 48.dp),
                contentDescription = stringResource(R.string.board_back),
            ) { BackIcon() }
            Text(stringResource(R.string.sticker_album_title), style = AppType.h1.copy(fontSize = 28.sp))
        }

        Text(
            pluralStringResource(
                R.plurals.sticker_album_count,
                progress.stickers.size,
                progress.stickers.size,
                Stickers.all.size,
            ),
            style = AppType.body.copy(color = Ink.inkSoft),
        )

        StickerGrid(progress, selected, onSelect)
        StickerCaption(progress, selected)
    }
}

/**
 * A plain grid rather than a lazy one: the whole app already lives inside a vertically
 * scrolling column, which a lazy grid cannot be measured inside, and sixteen slots have
 * nothing to be lazy about.
 */
@Composable
private fun StickerGrid(progress: Progress, selected: StickerId?, onSelect: (StickerId) -> Unit) {
    SketchSurface(Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Stickers.all.chunked(COLUMNS).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { sticker ->
                        Slot(
                            sticker = sticker,
                            unlocked = sticker.id in progress.stickers,
                            selected = sticker.id == selected,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelect(sticker.id) },
                        )
                    }
                    // Keeps a short last row aligned with the ones above it.
                    repeat(COLUMNS - row.size) { Box(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun Slot(
    sticker: Sticker,
    unlocked: Boolean,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    // Locked slots describe what would earn them, so a screen reader hears sixteen different
    // invitations rather than sixteen identical "locked"s.
    val label = if (unlocked) stringResource(sticker.name) else sticker.hint.resolve()
    Box(
        modifier
            .aspectRatio(1f)
            .then(
                if (unlocked) {
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Ink.white)
                        .innerOutline(if (selected) Ink.ink else Ink.grid, if (selected) 3.dp else 2.dp, 14.dp)
                } else {
                    Modifier.dashedRoundBox(
                        outline = if (selected) Ink.ink else Ink.inkSoft,
                        fill = Color.Transparent,
                        dashed = true,
                        width = 2.dp,
                        corner = 14.dp,
                    )
                },
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        StickerIcon(sticker.id, unlocked = unlocked, size = 44.dp)
    }
}

/** Where the unlock rule is spelled out, in the same spot the board explains a tapped tile. */
@Composable
private fun StickerCaption(progress: Progress, selected: StickerId?) {
    Box(Modifier.fillMaxWidth().heightIn(min = 54.dp), contentAlignment = Alignment.TopCenter) {
        if (selected == null) {
            Text(
                stringResource(R.string.sticker_tap_hint),
                style = AppType.h2.copy(fontSize = 19.sp, lineHeight = 25.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        } else {
            val sticker = Stickers[selected]
            val unlocked = selected in progress.stickers
            Column(Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(sticker.name),
                    style = AppType.h2.copy(fontSize = 19.sp, lineHeight = 25.sp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    if (unlocked) stringResource(R.string.sticker_got_it) else sticker.hint.resolve(),
                    style = AppType.body.copy(color = Ink.inkSoft),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
