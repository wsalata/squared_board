package eu.tudek.squared_board.game

import androidx.annotation.StringRes
import eu.tudek.squared_board.R
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.StickerId

enum class Screen { HOME, GAME, RESULT, BOARD, STICKERS }

enum class Mode { ADVENTURE, RACE, DAILY }

const val ROUND = 10
const val RACE_MS = 60_000L

/** The challenge of the day is deliberately short: five questions a child can fit in anywhere. */
const val DAILY_ROUND = 5

/** How many days the streak strip on the home screen shows. */
const val STRIP_DAYS = 7

/** Live state of a round. */
data class GameState(
    val mode: Mode,
    val question: Question,
    val choices: List<Int> = emptyList(),
    /** How many questions this round has; the race ignores it and runs on the clock. */
    val total: Int = ROUND,
    val index: Int = 0,
    /** The 1-based number of the question on screen; unlike [index] it waits for the next question. */
    val shown: Int = 1,
    val correct: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    /** One slot per question of a counted round: null = not reached yet. */
    val results: List<Boolean?> = List(total) { null },
    val wrong: List<Question> = emptyList(),
    val locked: Boolean = false,
    val typed: String = "",
    val chosen: Int? = null,
    val feedback: UiText? = null,
    val feedbackOk: Boolean? = null,
    val showNext: Boolean = false,
    @StringRes val nextLabel: Int = R.string.game_next,
    val millisLeft: Long = RACE_MS,
    /** Bumped on a wrong answer to retrigger the card's shake. */
    val shakeTick: Int = 0,
    val finished: Boolean = false,
) {
    /** The answer digits shown in the blank, or null while it is still empty. */
    val blankText: String
        get() = when {
            locked -> question.ans.toString()
            typed.isNotEmpty() -> typed
            else -> "?"
        }
}

data class ResultState(
    val mode: Mode,
    val stars: Int,
    val correct: Int,
    val bestRace: Int,
    val newRecord: Boolean,
    val wrong: List<String>,
    val confetti: Boolean,
    val total: Int = ROUND,
    /** The extra star the challenge of the day pays out, on top of the round's own stars. */
    val bonusStar: Boolean = false,
    val newStickers: List<StickerId> = emptyList(),
    /** Days in a row after this round, so the result screen can say it out loud. */
    val streak: Int = 0,
) {
    @get:StringRes
    val title: Int
        get() = if (newRecord && correct > 0) R.string.result_new_record else when (stars) {
            3 -> R.string.result_stars_3
            2 -> R.string.result_stars_2
            1 -> R.string.result_stars_1
            else -> R.string.result_stars_0
        }

    val summary: UiText
        get() = if (mode == Mode.RACE) {
            UiText.Quantity(R.plurals.result_summary_race, correct, listOf(correct, bestRace))
        } else {
            UiText.Res(R.string.result_summary_adventure, listOf(correct, total, stars))
        }
}

/**
 * The home screen's view of the calendar: the last [STRIP_DAYS] days, which of them fall
 * inside the live streak, and whether today's challenge is already done.
 */
data class DailyState(
    val streak: Int = 0,
    /** Oldest first; the last entry is today. */
    val days: List<Boolean> = List(STRIP_DAYS) { false },
    /** Epoch day of the first box, so the strip can label its columns. */
    val firstDay: Int = 0,
    val challengeDone: Boolean = false,
) {
    companion object {
        val EMPTY = DailyState()

        /**
         * The strip is worked out from the last played day and the streak length rather than
         * from a stored history of every day played: a streak is unbroken by definition, so
         * those two numbers already describe it, and nothing has to be pruned as weeks pass.
         */
        fun from(p: Progress, today: Int): DailyState {
            val streak = p.streakOn(today)
            val first = today - (STRIP_DAYS - 1)
            val from = p.lastPlayedDay - streak + 1
            return DailyState(
                streak = streak,
                days = List(STRIP_DAYS) { i -> streak > 0 && (first + i) in from..p.lastPlayedDay },
                firstDay = first,
                challengeDone = p.challengeDone(today),
            )
        }
    }
}
