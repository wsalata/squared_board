package eu.tudek.squared_board

import eu.tudek.squared_board.game.plural
import org.junit.Assert.assertEquals
import org.junit.Test

class PolishTest {

    private fun form(n: Int) = plural(n, "działanie", "działania", "działań")

    @Test
    fun `singular, few and many forms`() {
        assertEquals("działanie", form(1))
        assertEquals("działania", form(2))
        assertEquals("działania", form(4))
        assertEquals("działań", form(5))
        assertEquals("działań", form(0))
        assertEquals("działań", form(11))
    }

    @Test
    fun `the teens take the many form even though they end in two to four`() {
        assertEquals("działań", form(12))
        assertEquals("działań", form(13))
        assertEquals("działań", form(14))
        assertEquals("działania", form(22))
        assertEquals("działania", form(104))
    }
}
