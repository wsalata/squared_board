package eu.tudek.squared_board

import eu.tudek.squared_board.data.OpMode
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.Settings
import eu.tudek.squared_board.game.AnswerKind
import eu.tudek.squared_board.game.QuestionGenerator
import eu.tudek.squared_board.game.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuestionGeneratorTest {

    private fun generator(op: OpMode, tables: List<Int> = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10)): QuestionGenerator {
        val random = Random(1234)
        return QuestionGenerator(Settings(op = op, tables = tables), { Progress() }) { random.nextDouble() }
    }

    /** The blank always holds the answer, so filling it in must produce a true statement. */
    private fun assertEquationHolds(tokens: List<Token>, ans: Int) {
        val values = tokens.map {
            when (it) {
                is Token.Num -> it.value
                Token.Blank -> ans
                is Token.Sym -> null
            }
        }
        val symbols = tokens.filterIsInstance<Token.Sym>().map { it.text }
        val (left, op, right, _, result) = listOf(values[0], null, values[2], null, values[4])
        val computed = if (symbols[0] == "·") left!! * right!! else left!! / right!!
        assertEquals("$tokens", result, computed)
        assertTrue("$tokens", op == null)
    }

    @Test
    fun `multiplication questions ask for the product`() {
        val gen = generator(OpMode.MUL)
        repeat(200) {
            val q = gen.next()
            assertEquals(AnswerKind.PRODUCT, q.kind)
            assertEquals(q.a * q.b, q.ans)
            assertEquals(Token.Blank, q.tokens.last())
            assertEquationHolds(q.tokens, q.ans)
        }
    }

    @Test
    fun `division questions ask for the other factor`() {
        val gen = generator(OpMode.DIV)
        repeat(200) {
            val q = gen.next()
            assertEquals(AnswerKind.FACTOR, q.kind)
            assertEquals(q.b, q.ans)
            assertEquationHolds(q.tokens, q.ans)
        }
    }

    @Test
    fun `puzzle mode hides a different part of the equation each time`() {
        val gen = generator(OpMode.MISS)
        val blankPositions = mutableSetOf<Int>()
        repeat(400) {
            val q = gen.next()
            blankPositions.add(q.tokens.indexOf(Token.Blank))
            assertEquationHolds(q.tokens, q.ans)
        }
        // The prototype offers four shapes: a missing factor on either side, or a missing
        // dividend or divisor.
        assertEquals(setOf(0, 2), blankPositions)
    }

    @Test
    fun `mixed mode produces both multiplication and division`() {
        val gen = generator(OpMode.MIX)
        val symbols = mutableSetOf<String>()
        repeat(200) { symbols.add((gen.next().tokens[1] as Token.Sym).text) }
        assertEquals(setOf("·", ":"), symbols)
    }

    @Test
    fun `only the chosen tables come up`() {
        val gen = generator(OpMode.MUL, tables = listOf(7))
        repeat(200) {
            val q = gen.next()
            assertEquals(7, q.a)
            assertTrue("factor ${q.b} out of range", q.b in 1..10)
        }
    }

    @Test
    fun `four distinct plausible options, always including the answer`() {
        val gen = generator(OpMode.MUL)
        repeat(200) {
            val q = gen.next()
            val choices = gen.choices(q)
            assertEquals(4, choices.size)
            assertEquals(4, choices.toSet().size)
            assertTrue("$choices misses ${q.ans}", q.ans in choices)
            assertTrue("$choices out of range", choices.all { it in 1..100 })
        }
    }

    @Test
    fun `recently asked facts are pushed to the back of the queue`() {
        // The last four facts keep a 0.04 weight rather than zero, so a repeat stays possible
        // but should be rare even with only twenty facts to draw from.
        val gen = generator(OpMode.MUL, tables = listOf(3, 4))
        var previous = gen.next()
        var repeats = 0
        val draws = 2000
        repeat(draws) {
            val q = gen.next()
            if (Progress.tileKey(q.a, q.b) == Progress.tileKey(previous.a, previous.b)) repeats++
            previous = q
        }
        assertTrue("$repeats repeats in $draws draws", repeats < draws / 50)
    }

    @Test
    fun `weak facts are drilled more often than strong ones`() {
        var progress = Progress()
        repeat(5) { progress = progress.withAnswer(4, 6, correct = true) }
        val random = Random(99)
        val gen = QuestionGenerator(Settings(op = OpMode.MUL, tables = listOf(4)), { progress }) { random.nextDouble() }
        var mastered = 0
        var weak = 0
        repeat(2000) {
            val q = gen.next()
            if (q.b == 6) mastered++ else if (q.b == 7) weak++
        }
        assertTrue("mastered=$mastered weak=$weak", weak > mastered * 2)
    }
}
