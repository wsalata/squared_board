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
    /** Epoch day of the last day a round was finished; 0 means never. */
    val lastPlayedDay: Int = 0,
    /** Days in a row up to and including [lastPlayedDay]. */
    val streak: Int = 0,
    val bestStreak: Int = 0,
    /** Epoch day the challenge of the day was last completed. */
    val dailyDoneDay: Int = 0,
    val stickers: Set<StickerId> = emptySet(),
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

    /** The whole table of [n]: all ten of its facts known. */
    fun tableMastered(n: Int): Boolean = (1..10).all { score(n, it) >= 4 }

    /**
     * The streak as it stands on [today]. The stored [streak] goes stale the moment the day
     * turns over, so nothing reads it directly.
     */
    fun streakOn(today: Int): Int = if (today - lastPlayedDay in 0..1) streak else 0

    /** The 0 guard keeps "never done" from colliding with "done on epoch day zero". */
    fun challengeDone(today: Int): Boolean = dailyDoneDay != 0 && dailyDoneDay == today

    /**
     * Books [today] as practised. Only called once a round has actually been finished.
     *
     * A day already counted changes nothing, and so does a clock that has moved backwards —
     * a child flying west, or a parent correcting the date, must never lose their streak.
     */
    fun withDayPlayed(today: Int): Progress = when {
        today <= lastPlayedDay -> this
        today == lastPlayedDay + 1 -> bumped(today, streak + 1)
        else -> bumped(today, 1)
    }

    private fun bumped(today: Int, n: Int) =
        copy(lastPlayedDay = today, streak = n, bestStreak = maxOf(bestStreak, n))

    companion object {
        fun tileKey(a: Int, b: Int) = if (a <= b) "${a}x$b" else "${b}x$a"
    }
}
