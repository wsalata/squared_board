package eu.tudek.squared_board.data

/** What the player is practising. */
enum class OpMode { MUL, DIV, MIX, MISS }

/** How the player answers: tapping one of four results, or typing it. */
enum class InputMode { CHOICE, KEYPAD }

data class Settings(
    val op: OpMode = OpMode.MUL,
    val tables: List<Int> = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10),
    val input: InputMode = InputMode.CHOICE,
    val sound: Boolean = true,
)

/**
 * Everything that survives between sessions.
 *
 * [tiles] maps a fact to a mastery score of 0..5, keyed so that 3x4 and 4x3 share one entry.
 */
data class Progress(
    val tiles: Map<String, Int> = emptyMap(),
    val stars: Int = 0,
    val bestRace: Int = 0,
    val settings: Settings = Settings(),
) {
    fun score(a: Int, b: Int): Int = tiles[tileKey(a, b)] ?: 0

    /** A fact counts as learned once it has been answered right four times in a row's worth of tries. */
    fun mastered(): Int {
        var n = 0
        for (r in 1..10) for (c in 1..10) if (score(r, c) >= 4) n++
        return n
    }

    /** One right answer earns a step; a wrong one costs two. */
    fun withAnswer(a: Int, b: Int, correct: Boolean): Progress {
        val k = tileKey(a, b)
        val s = tiles[k] ?: 0
        val next = if (correct) minOf(5, s + 1) else maxOf(0, s - 2)
        return copy(tiles = tiles + (k to next))
    }

    companion object {
        fun tileKey(a: Int, b: Int) = if (a <= b) "${a}x$b" else "${b}x$a"
    }
}
