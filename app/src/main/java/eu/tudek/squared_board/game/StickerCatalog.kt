package eu.tudek.squared_board.game

import androidx.annotation.StringRes
import eu.tudek.squared_board.R
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.StickerId

/**
 * One sticker: what it is called, what earns it in words a child can read, and the rule itself.
 *
 * Every rule must be monotone in [Progress] — a sticker is never taken back. That is why the
 * streak rules read `bestStreak` rather than the live streak: a missed day costs the row of
 * ticks, never a sticker already in the album.
 */
data class Sticker(
    val id: StickerId,
    @StringRes val name: Int,
    val hint: UiText,
    val earned: (Progress) -> Boolean,
)

object Stickers {

    private fun table(n: Int, id: StickerId, @StringRes name: Int) = Sticker(
        id = id,
        name = name,
        hint = UiText.Res(R.string.sticker_hint_table, listOf(n)),
        earned = { it.tableMastered(n) },
    )

    private fun stars(id: StickerId, @StringRes name: Int, n: Int) = Sticker(
        id = id,
        name = name,
        hint = UiText.Res(R.string.sticker_hint_stars, listOf(n)),
        earned = { it.stars >= n },
    )

    private fun streak(id: StickerId, @StringRes name: Int, days: Int) = Sticker(
        id = id,
        name = name,
        hint = UiText.Quantity(R.plurals.sticker_hint_streak, days, listOf(days)),
        earned = { it.bestStreak >= days },
    )

    /**
     * Four families, each one sentence long. The ten table stickers carry the album because
     * they are the only rule the player can watch themselves approach, row by row, on the board.
     */
    val all: List<Sticker> = listOf(
        table(1, StickerId.TABLE_1, R.string.sticker_name_rocket),
        table(2, StickerId.TABLE_2, R.string.sticker_name_cat),
        table(3, StickerId.TABLE_3, R.string.sticker_name_icecream),
        table(4, StickerId.TABLE_4, R.string.sticker_name_dino),
        table(5, StickerId.TABLE_5, R.string.sticker_name_butterfly),
        table(6, StickerId.TABLE_6, R.string.sticker_name_robot),
        table(7, StickerId.TABLE_7, R.string.sticker_name_rainbow),
        table(8, StickerId.TABLE_8, R.string.sticker_name_cupcake),
        table(9, StickerId.TABLE_9, R.string.sticker_name_dragon),
        table(10, StickerId.TABLE_10, R.string.sticker_name_crown),
        stars(StickerId.STARS_10, R.string.sticker_name_sun, 10),
        stars(StickerId.STARS_50, R.string.sticker_name_balloon, 50),
        Sticker(
            id = StickerId.BOARD_FULL,
            name = R.string.sticker_name_medal,
            hint = UiText.Res(R.string.sticker_hint_board_full),
            earned = { it.mastered() >= 100 },
        ),
        Sticker(
            id = StickerId.RACE_20,
            name = R.string.sticker_name_train,
            hint = UiText.Res(R.string.sticker_hint_race, listOf(20)),
            earned = { it.bestRace >= 20 },
        ),
        streak(StickerId.STREAK_3, R.string.sticker_name_flower, 3),
        streak(StickerId.STREAK_7, R.string.sticker_name_cake, 7),
    )

    private val byId = all.associateBy { it.id }

    operator fun get(id: StickerId): Sticker = byId.getValue(id)
}

/**
 * Banks every sticker now earned and reports the ones that are new.
 *
 * The album is stored rather than derived because a wrong answer costs two mastery points:
 * a derived album would hand out a sticker and take it away again the next time the child
 * slipped on 7 x 8.
 */
fun Progress.awardStickers(): Pair<Progress, List<StickerId>> {
    val fresh = Stickers.all.filter { it.id !in stickers && it.earned(this) }.map { it.id }
    return if (fresh.isEmpty()) this to emptyList() else copy(stickers = stickers + fresh) to fresh
}
