package eu.tudek.squared_board

import eu.tudek.squared_board.data.Progress
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressTest {

    @Test
    fun `a fact is stored once regardless of factor order`() {
        val p = Progress().withAnswer(3, 4, correct = true)
        assertEquals(1, p.score(3, 4))
        assertEquals(1, p.score(4, 3))
        assertEquals(1, p.tiles.size)
    }

    @Test
    fun `right answers climb by one and stop at five`() {
        var p = Progress()
        repeat(7) { p = p.withAnswer(6, 7, correct = true) }
        assertEquals(5, p.score(6, 7))
    }

    @Test
    fun `a wrong answer costs two steps but never goes below zero`() {
        var p = Progress()
        repeat(3) { p = p.withAnswer(8, 9, correct = true) }
        assertEquals(3, p.score(8, 9))
        p = p.withAnswer(8, 9, correct = false)
        assertEquals(1, p.score(8, 9))
        p = p.withAnswer(8, 9, correct = false)
        assertEquals(0, p.score(8, 9))
    }

    @Test
    fun `mastery starts at four correct answers`() {
        var p = Progress()
        repeat(3) { p = p.withAnswer(2, 5, correct = true) }
        assertEquals(0, p.mastered())
        p = p.withAnswer(2, 5, correct = true)
        // 2x5 and 5x2 are separate squares on the board, so the count goes straight to two.
        assertEquals(2, p.mastered())
    }

    @Test
    fun `the mastered count lights both a fact and its mirror`() {
        var p = Progress()
        repeat(4) { p = p.withAnswer(3, 7, correct = true) }
        // 3x7 and 7x3 are both filled on the board, so one learned fact lights two squares.
        assertEquals(2, p.mastered())
    }
}
