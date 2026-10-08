package eu.tudek.squared_board

import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.game.DailyState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The calendar side of the game, which is why it all lives in pure functions on [Progress]:
 * every day transition can be checked here without a clock, a view model or a device.
 */
class StreakTest {

    @Test
    fun `the first day played starts a streak of one`() {
        val p = Progress().withDayPlayed(100)
        assertEquals(1, p.streak)
        assertEquals(1, p.bestStreak)
        assertEquals(100, p.lastPlayedDay)
    }

    @Test
    fun `playing on consecutive days extends the streak`() {
        val p = Progress().withDayPlayed(100).withDayPlayed(101).withDayPlayed(102)
        assertEquals(3, p.streak)
        assertEquals(3, p.bestStreak)
    }

    @Test
    fun `a second round on the same day changes nothing`() {
        val once = Progress().withDayPlayed(100)
        assertEquals(once, once.withDayPlayed(100))
    }

    @Test
    fun `a missed day starts the streak again but keeps the best`() {
        val p = Progress().withDayPlayed(100).withDayPlayed(101).withDayPlayed(104)
        assertEquals(1, p.streak)
        assertEquals(2, p.bestStreak)
        assertEquals(104, p.lastPlayedDay)
    }

    /** A child whose device clock moves backwards must not be punished for it. */
    @Test
    fun `a clock moved backwards takes nothing away`() {
        val p = Progress().withDayPlayed(100).withDayPlayed(101)
        assertEquals(p, p.withDayPlayed(95))
    }

    @Test
    fun `the streak stays alive through today and yesterday, then lapses`() {
        val p = Progress(lastPlayedDay = 100, streak = 5)
        assertEquals(5, p.streakOn(100))
        assertEquals(5, p.streakOn(101))
        assertEquals(0, p.streakOn(102))
        assertEquals(0, p.streakOn(130))
    }

    @Test
    fun `a progress that has never been played has no streak`() {
        assertEquals(0, Progress().streakOn(19_000))
    }

    @Test
    fun `the challenge is done only on the day it was done`() {
        val p = Progress(dailyDoneDay = 100)
        assertTrue(p.challengeDone(100))
        assertFalse(p.challengeDone(101))
        assertFalse(Progress().challengeDone(0))
    }

    @Test
    fun `the strip ticks the streak up to today`() {
        val p = Progress(lastPlayedDay = 102, streak = 3)
        val strip = DailyState.from(p, today = 102)
        assertEquals(3, strip.streak)
        assertEquals(listOf(false, false, false, false, true, true, true), strip.days)
        assertEquals(96, strip.firstDay)
    }

    @Test
    fun `today sits empty while the streak from yesterday still stands`() {
        val p = Progress(lastPlayedDay = 102, streak = 3)
        val strip = DailyState.from(p, today = 103)
        assertEquals(3, strip.streak)
        assertEquals(listOf(false, false, false, true, true, true, false), strip.days)
    }

    @Test
    fun `a lapsed streak empties the strip`() {
        val strip = DailyState.from(Progress(lastPlayedDay = 102, streak = 3), today = 110)
        assertEquals(0, strip.streak)
        assertTrue(strip.days.none { it })
    }

    @Test
    fun `a streak longer than the strip fills every box`() {
        val strip = DailyState.from(Progress(lastPlayedDay = 200, streak = 20), today = 200)
        assertTrue(strip.days.all { it })
    }
}
