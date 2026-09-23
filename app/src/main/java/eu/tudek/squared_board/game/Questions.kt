package eu.tudek.squared_board.game

import eu.tudek.squared_board.data.OpMode
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.Settings

/** One piece of the rendered equation. */
sealed interface Token {
    data class Num(val value: Int) : Token
    data class Sym(val text: String) : Token
    data object Blank : Token
}

/** The localised words a screen reader needs to read an equation out loud. */
data class SpokenWords(
    val times: String,
    val dividedBy: String,
    val equals: String,
    val blank: String,
)

/** Whether the missing number is the product or one of the factors. */
enum class AnswerKind { PRODUCT, FACTOR }

data class Question(
    val a: Int,
    val b: Int,
    val tokens: List<Token>,
    val ans: Int,
    val kind: AnswerKind,
    /** The visible factor, used to seed believable wrong answers. */
    val other: Int? = null,
) {
    /** "3 · 4 = 12" — the whole equation with the blank filled in. */
    fun fullText(): String = tokens.joinToString(" ") {
        when (it) {
            is Token.Num -> it.value.toString()
            is Token.Sym -> it.text
            Token.Blank -> ans.toString()
        }
    }

    /** Spoken form for screen readers; the words come from the UI so they follow the locale. */
    fun spoken(words: SpokenWords): String = tokens.joinToString(" ") {
        when (it) {
            is Token.Num -> it.value.toString()
            is Token.Sym -> when (it.text) {
                "·" -> words.times
                ":" -> words.dividedBy
                "=" -> words.equals
                else -> it.text
            }
            Token.Blank -> words.blank
        }
    }
}

private fun num(v: Int) = Token.Num(v)
private fun sym(s: String) = Token.Sym(s)

/**
 * Draws facts the player is weakest at more often, avoids the last four, and keeps the
 * trivial ×1 row rare unless it is the only table selected.
 */
class QuestionGenerator(
    private val settings: Settings,
    private val progress: () -> Progress,
    private val random: () -> Double = Math::random,
) {
    private val recent = ArrayDeque<String>()

    private fun pickFact(): Pair<Int, Int> {
        val p = progress()
        val weighted = ArrayList<Triple<Int, Int, Double>>(settings.tables.size * 10)
        var total = 0.0
        for (t in settings.tables) {
            for (x in 1..10) {
                var w = 1 + (5 - p.score(t, x)) * 0.5
                if ((x == 1 || t == 1) && settings.tables.size > 1) w *= 0.35
                if (Progress.tileKey(t, x) in recent) w *= 0.04
                weighted.add(Triple(t, x, w))
                total += w
            }
        }
        var r = random() * total
        for ((t, x, w) in weighted) {
            r -= w
            if (r <= 0) return remember(t, x)
        }
        val last = weighted.last()
        return remember(last.first, last.second)
    }

    private fun remember(t: Int, x: Int): Pair<Int, Int> {
        recent.addLast(Progress.tileKey(t, x))
        while (recent.size > 4) recent.removeFirst()
        return t to x
    }

    fun next(): Question {
        val (t, x) = pickFact()
        val product = t * x
        val op = when (settings.op) {
            OpMode.MIX -> if (random() < 0.5) OpMode.MUL else OpMode.DIV
            else -> settings.op
        }
        return when (op) {
            OpMode.MUL -> {
                // Show the factors in either order so 7 · 3 turns up as often as 3 · 7.
                val (l, r) = if (random() < 0.5) t to x else x to t
                Question(t, x, listOf(num(l), sym("·"), num(r), sym("="), Token.Blank), product, AnswerKind.PRODUCT)
            }
            OpMode.DIV -> Question(
                t, x, listOf(num(product), sym(":"), num(t), sym("="), Token.Blank), x, AnswerKind.FACTOR, other = t,
            )
            else -> when ((random() * 4).toInt().coerceIn(0, 3)) {
                0 -> Question(t, x, listOf(Token.Blank, sym("·"), num(t), sym("="), num(product)), x, AnswerKind.FACTOR, other = t)
                1 -> Question(t, x, listOf(num(t), sym("·"), Token.Blank, sym("="), num(product)), x, AnswerKind.FACTOR, other = t)
                2 -> Question(t, x, listOf(Token.Blank, sym(":"), num(t), sym("="), num(x)), product, AnswerKind.PRODUCT)
                else -> Question(t, x, listOf(num(product), sym(":"), Token.Blank, sym("="), num(x)), t, AnswerKind.FACTOR, other = x)
            }
        }
    }

    /** Four options: the answer plus near-misses a child would plausibly reach for. */
    fun choices(q: Question): List<Int> {
        val candidates = if (q.kind == AnswerKind.PRODUCT) {
            listOf(
                q.a * (q.b + 1), q.a * (q.b - 1), (q.a + 1) * q.b, (q.a - 1) * q.b,
                q.ans + 1, q.ans - 1, q.ans + 2, q.ans + 10, q.ans - 10,
            )
        } else {
            listOfNotNull(q.ans + 1, q.ans - 1, q.ans + 2, q.ans - 2, q.other)
        }
        val out = mutableListOf(q.ans)
        for (v in candidates.shuffled()) {
            if (out.size == 4) break
            if (v > 0 && v <= 100 && v !in out) out.add(v)
        }
        var guard = 0
        while (out.size < 4 && guard++ < 200) {
            val v = if (q.kind == AnswerKind.PRODUCT) 1 + (random() * 100).toInt() else 1 + (random() * 12).toInt()
            if (v !in out) out.add(v)
        }
        return out.shuffled()
    }
}
