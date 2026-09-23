package eu.tudek.squared_board.game

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

/**
 * Text decided outside composition — in the view model or the game model — that still has to
 * follow the device language. It carries the resource id and its arguments until a composable
 * can resolve them, so no layer below the UI needs a Context.
 */
sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    data class Quantity(
        @PluralsRes val id: Int,
        val count: Int,
        val args: List<Any> = emptyList(),
    ) : UiText
}

@Composable
fun UiText.resolve(): String = when (this) {
    is UiText.Res -> stringResource(id, *args.toTypedArray())
    is UiText.Quantity -> pluralStringResource(id, count, *args.toTypedArray())
}
