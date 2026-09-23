package eu.tudek.squared_board

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.Settings
import eu.tudek.squared_board.game.Mode
import eu.tudek.squared_board.game.ResultState
import eu.tudek.squared_board.ui.HomeScreen
import eu.tudek.squared_board.ui.ResultScreen
import eu.tudek.squared_board.ui.theme.SquaredBoardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The rest of the render tests run under the Polish qualifier. These run without one, so they
 * prove the default resources in values/ are the English ones a non-Polish phone will get —
 * including the plural forms, which differ in shape between the two languages.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w411dp-h891dp-xxhdpi")
class LocalizationTest {

    @get:Rule
    val compose = createComposeRule()

    private fun progress(vararg learned: Pair<Int, Int>): Progress {
        var p = Progress(settings = Settings())
        for ((a, b) in learned) repeat(5) { p = p.withAnswer(a, b, true) }
        return p
    }

    @Test
    fun `the home screen falls back to English`() {
        compose.setContent {
            SquaredBoardTheme {
                HomeScreen(
                    progress = progress(3 to 4, 6 to 7),
                    onToggleSound = {},
                    onOpenBoard = {},
                    onOp = {},
                    onToggleTable = {},
                    onSelectAll = {},
                    onInput = {},
                    onPlay = {},
                )
            }
        }
        compose.onNodeWithText("Times Tables\nGrid").assertIsDisplayed()
        compose.onNodeWithText("Multiplication").assertIsDisplayed()
        compose.onNodeWithText("Race the clock").assertIsDisplayed()
        // The same four squares as the Polish test, worded with the English "other" form.
        compose.onNodeWithText("You know 4 facts out of 100. Keep playing to fill the board.")
            .assertIsDisplayed()
        compose.onNodeWithContentDescription("Show my board").assertIsDisplayed()
    }

    @Test
    fun `the race summary uses the English singular for one correct answer`() {
        compose.setContent {
            SquaredBoardTheme {
                ResultScreen(
                    state = ResultState(
                        mode = Mode.RACE,
                        stars = 1,
                        correct = 1,
                        bestRace = 9,
                        newRecord = false,
                        wrong = emptyList(),
                        confetti = false,
                    ),
                    animationsOn = false,
                    onAgain = {},
                    onMenu = {},
                )
            }
        }
        compose.onNodeWithText("Good start!").assertIsDisplayed()
        compose.onNodeWithText("1 correct answer in 60 seconds. Best: 9.").assertIsDisplayed()
    }
}
