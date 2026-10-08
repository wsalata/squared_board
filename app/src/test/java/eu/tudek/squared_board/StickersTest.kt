package eu.tudek.squared_board

import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.StickerId
import eu.tudek.squared_board.game.Stickers
import eu.tudek.squared_board.game.awardStickers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StickersTest {

    /** Four right answers take a fact to the mastery threshold. */
    private fun Progress.learn(a: Int, b: Int): Progress {
        var p = this
        repeat(4) { p = p.withAnswer(a, b, correct = true) }
        return p
    }

    private fun learnTable(n: Int): Progress {
        var p = Progress()
        for (x in 1..10) p = p.learn(n, x)
        return p
    }

    /**
     * A catalogue entry missing for some id would draw nothing at all and fail silently,
     * so the two lists are pinned to each other here.
     */
    @Test
    fun `every sticker id has exactly one catalogue entry`() {
        assertEquals(StickerId.entries.toSet(), Stickers.all.map { it.id }.toSet())
        assertEquals(StickerId.entries.size, Stickers.all.size)
    }

    @Test
    fun `a whole table earns its sticker and nothing else`() {
        val (p, fresh) = learnTable(7).awardStickers()
        assertEquals(listOf(StickerId.TABLE_7), fresh)
        assertTrue(StickerId.TABLE_7 in p.stickers)
    }

    @Test
    fun `nine facts out of ten are not a whole table`() {
        var p = Progress()
        for (x in 1..9) p = p.learn(7, x)
        assertFalse(p.tableMastered(7))
        assertTrue(p.awardStickers().second.isEmpty())
    }

    @Test
    fun `the star ladder, the race and the board each pay out`() {
        assertEquals(listOf(StickerId.STARS_10), Progress(stars = 10).awardStickers().second)
        assertEquals(
            listOf(StickerId.STARS_10, StickerId.STARS_50),
            Progress(stars = 50).awardStickers().second,
        )
        assertEquals(listOf(StickerId.RACE_20), Progress(bestRace = 22).awardStickers().second)
    }

    @Test
    fun `a week-long streak earns both streak stickers`() {
        assertEquals(
            listOf(StickerId.STREAK_3, StickerId.STREAK_7),
            Progress(bestStreak = 7).awardStickers().second,
        )
    }

    @Test
    fun `awarding twice reports nothing the second time`() {
        val (p, first) = learnTable(3).awardStickers()
        assertTrue(first.isNotEmpty())
        val (again, second) = p.awardStickers()
        assertTrue(second.isEmpty())
        assertEquals(p, again)
    }

    /**
     * The reason the album is stored rather than derived: a wrong answer costs two mastery
     * points, so a derived album would hand a sticker back the next time a child slipped.
     */
    @Test
    fun `forgetting a fact does not take the sticker away`() {
        var p = learnTable(7).awardStickers().first
        repeat(3) { p = p.withAnswer(7, 8, correct = false) }
        assertEquals(0, p.score(7, 8))
        assertFalse(p.tableMastered(7))
        assertTrue(StickerId.TABLE_7 in p.stickers)
        assertTrue(p.awardStickers().second.isEmpty())
    }

    /** A broken streak costs the row of ticks, never a sticker already in the album. */
    @Test
    fun `a lapsed streak keeps its sticker`() {
        var p = Progress()
        repeat(3) { day -> p = p.withDayPlayed(100 + day) }
        p = p.awardStickers().first
        assertTrue(StickerId.STREAK_3 in p.stickers)

        p = p.withDayPlayed(200)
        assertEquals(1, p.streak)
        assertTrue(StickerId.STREAK_3 in p.stickers)
    }

    @Test
    fun `a full board earns the medal`() {
        var p = Progress()
        for (r in 1..10) for (c in 1..10) p = p.learn(r, c)
        assertEquals(100, p.mastered())
        assertTrue(StickerId.BOARD_FULL in p.awardStickers().first.stickers)
    }
}
