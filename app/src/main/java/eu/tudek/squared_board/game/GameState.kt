package eu.tudek.squared_board.game

import androidx.annotation.StringRes
import eu.tudek.squared_board.R

enum class Screen { HOME, GAME, RESULT, BOARD }

enum class Mode { ADVENTURE, RACE }

const val ROUND = 10
const val RACE_MS = 60_000L

/** Live state of a round. */
data class GameState(
    val mode: Mode,
    val question: Question,
    val choices: List<Int> = emptyList(),
    val index: Int = 0,
    /** The 1-based number of the question on screen; unlike [index] it waits for the next question. */
    val shown: Int = 1,
    val correct: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    /** One slot per question of an adventure round: null = not reached yet. */
    val results: List<Boolean?> = List(ROUND) { null },
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
            UiText.Res(R.string.result_summary_adventure, listOf(correct, ROUND, stars))
        }
}
