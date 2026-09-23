package eu.tudek.squared_board

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import eu.tudek.squared_board.ui.SquaredBoardApp
import eu.tudek.squared_board.ui.theme.SquaredBoardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Drives the whole app — shell, view model and stored progress together — to catch anything
 * that only breaks once the real pieces are wired up.
 */
@RunWith(RobolectricTestRunner::class)
// Polski kwalifikator: te testy sprawdzają tłumaczenie z values-pl/.
@Config(sdk = [34], qualifiers = "pl-rPL-w411dp-h891dp-xxhdpi")
class AppFlowTest {

    @get:Rule
    val compose = createComposeRule()

    private fun launch() {
        compose.setContent {
            SquaredBoardTheme { SquaredBoardApp(animationsOn = false) }
        }
    }

    @Test
    fun `the app starts on the menu`() {
        launch()
        compose.onNodeWithText("Tabliczka\nw kratkę").assertIsDisplayed()
        compose.onNodeWithText("Zagraj").assertIsDisplayed()
    }

    @Test
    fun `starting an adventure round shows a question and returning lands back on the menu`() {
        launch()
        compose.onNodeWithText("Zagraj").performClick()
        compose.onNodeWithText("Pytanie 1 z 10").assertIsDisplayed()
        compose.onNodeWithText("0 dobrych").assertIsDisplayed()

        compose.onNodeWithContentDescription("Zakończ grę").performClick()
        compose.onNodeWithText("Zagraj").assertIsDisplayed()
    }

    @Test
    fun `the race starts on a full clock`() {
        launch()
        compose.onNodeWithText("Wyścig z czasem").performClick()
        compose.onNodeWithText("Dobrze: 0").assertIsDisplayed()
        compose.onNodeWithText("60 s").assertIsDisplayed()
        compose.onNodeWithText("Pytanie 1 z 10").assertDoesNotExist()
    }

    @Test
    fun `the board opens from the progress card and goes back`() {
        launch()
        compose.onNodeWithContentDescription("Pokaż moją tabliczkę").performClick()
        compose.onNodeWithText("Moja tabliczka").assertIsDisplayed()
        compose.onNodeWithText("Dotknij kratkę, żeby zobaczyć działanie.").assertIsDisplayed()

        compose.onNodeWithContentDescription("9 razy 9 równa się 81").performClick()
        compose.onNodeWithText("9 · 9 = 81, 81 : 9 = 9").assertIsDisplayed()

        compose.onNodeWithContentDescription("Wróć do menu").performClick()
        compose.onNodeWithText("Zagraj").assertIsDisplayed()
    }

    @Test
    fun `switching to typed answers brings up the keypad`() {
        launch()
        compose.onNodeWithText("Wpisuję wynik").performClick()
        compose.onNodeWithText("Zagraj").performClick()
        compose.onNodeWithText("OK").assertIsDisplayed()
        compose.onNodeWithContentDescription("Usuń cyfrę").assertIsDisplayed()
    }

    @Test
    fun `choosing division changes the questions that come up`() {
        launch()
        compose.onNodeWithText("Dzielenie").performClick()
        compose.onNodeWithText("Zagraj").performClick()
        compose.onNodeWithText(":").assertIsDisplayed()
    }
}
