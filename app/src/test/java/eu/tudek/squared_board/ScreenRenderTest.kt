package eu.tudek.squared_board

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import eu.tudek.squared_board.data.InputMode
import eu.tudek.squared_board.data.OpMode
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.Settings
import eu.tudek.squared_board.game.AnswerKind
import eu.tudek.squared_board.game.GameState
import eu.tudek.squared_board.game.Mode
import eu.tudek.squared_board.game.Question
import eu.tudek.squared_board.game.ResultState
import eu.tudek.squared_board.game.Token
import eu.tudek.squared_board.ui.BoardScreen
import eu.tudek.squared_board.ui.GameScreen
import eu.tudek.squared_board.ui.HomeScreen
import eu.tudek.squared_board.ui.ResultScreen
import eu.tudek.squared_board.ui.theme.SquaredBoardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Renders each screen on the JVM to prove it composes, measures and draws without blowing up,
 * and that the pieces the player needs are actually on screen.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class ScreenRenderTest {

    @get:Rule
    val compose = createComposeRule()

    private fun progress(vararg learned: Pair<Int, Int>, settings: Settings = Settings()): Progress {
        var p = Progress(settings = settings)
        learned.forEach { (a, b) -> repeat(5) { p = p.withAnswer(a, b, correct = true) } }
        return p
    }

    private val question = Question(
        a = 3,
        b = 4,
        tokens = listOf(Token.Num(3), Token.Sym("·"), Token.Num(4), Token.Sym("="), Token.Blank),
        ans = 12,
        kind = AnswerKind.PRODUCT,
    )

    @Test
    fun `home screen shows the settings and both game modes`() {
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
        compose.onNodeWithText("Tabliczka\nw kratkę").assertIsDisplayed()
        compose.onNodeWithText("Mnożenie").assertIsDisplayed()
        compose.onNodeWithText("Zagadki").assertIsDisplayed()
        compose.onNodeWithText("Zagraj").assertIsDisplayed()
        compose.onNodeWithText("Wyścig z czasem").assertIsDisplayed()
        // Two learned facts light four squares: 3x4, 4x3, 6x7 and 7x6.
        compose.onNodeWithText("Umiesz 4 działania ze 100. Graj dalej, żeby zapełnić tabliczkę.")
            .assertIsDisplayed()
        compose.onNodeWithContentDescription("Pokaż moją tabliczkę").assertIsDisplayed()
    }

    @Test
    fun `home screen offers select-all only while tables are missing`() {
        compose.setContent {
            SquaredBoardTheme {
                HomeScreen(
                    progress = progress(settings = Settings(tables = (1..10).toList())),
                    onToggleSound = {}, onOpenBoard = {}, onOp = {},
                    onToggleTable = {}, onSelectAll = {}, onInput = {}, onPlay = {},
                )
            }
        }
        compose.onNodeWithText("Zaznacz wszystkie").assertDoesNotExist()
    }

    @Test
    fun `adventure game screen shows the equation and four answers`() {
        var answered: Int? = null
        compose.setContent {
            SquaredBoardTheme {
                GameScreen(
                    state = GameState(
                        mode = Mode.ADVENTURE,
                        question = question,
                        choices = listOf(12, 15, 9, 16),
                        correct = 3,
                    ),
                    input = InputMode.CHOICE,
                    animationsOn = false,
                    onQuit = {},
                    onAnswer = { answered = it },
                    onKey = {},
                    onNext = {},
                )
            }
        }
        compose.onNodeWithText("3").assertIsDisplayed()
        compose.onNodeWithText("·").assertIsDisplayed()
        compose.onNodeWithText("?").assertIsDisplayed()
        compose.onNodeWithText("Pytanie 1 z 10").assertIsDisplayed()
        compose.onNodeWithText("3 dobre").assertIsDisplayed()
        compose.onNodeWithContentDescription("Zakończ grę").assertIsDisplayed()

        compose.onNodeWithText("12").performClick()
        assert(answered == 12) { "tapping an answer must report it, got $answered" }
    }

    @Test
    fun `the keypad replaces the answer grid when typing`() {
        val pressed = mutableListOf<String>()
        compose.setContent {
            SquaredBoardTheme {
                GameScreen(
                    state = GameState(mode = Mode.ADVENTURE, question = question, typed = "1"),
                    input = InputMode.KEYPAD,
                    animationsOn = false,
                    onQuit = {}, onAnswer = {}, onKey = { pressed.add(it) }, onNext = {},
                )
            }
        }
        compose.onNodeWithText("OK").assertIsDisplayed()
        compose.onNodeWithContentDescription("Usuń cyfrę").assertIsDisplayed()
        // The typed digit shows in the blank rather than a question mark, so "1" appears
        // twice: once in the equation and once on the key.
        compose.onNodeWithText("?").assertDoesNotExist()
        compose.onAllNodesWithText("1").assertCountEquals(2)

        compose.onNodeWithText("7").performClick()
        compose.onNodeWithText("OK").performClick()
        assert(pressed == listOf("7", "ok")) { "expected [7, ok], got $pressed" }
    }

    @Test
    fun `a wrong answer reveals the fact and offers the next question`() {
        compose.setContent {
            SquaredBoardTheme {
                GameScreen(
                    state = GameState(
                        mode = Mode.ADVENTURE,
                        question = question,
                        choices = listOf(12, 15, 9, 16),
                        locked = true,
                        chosen = 15,
                        feedback = "Prawie! Zapamiętaj: 3 · 4 = 12",
                        feedbackOk = false,
                        showNext = true,
                        index = 1,
                    ),
                    input = InputMode.CHOICE,
                    animationsOn = false,
                    onQuit = {}, onAnswer = {}, onKey = {}, onNext = {},
                )
            }
        }
        compose.onNodeWithText("Prawie! Zapamiętaj: 3 · 4 = 12").assertIsDisplayed()
        compose.onNodeWithText("Dalej").assertIsDisplayed()
    }

    @Test
    fun `the race screen counts down instead of tracking questions`() {
        compose.setContent {
            SquaredBoardTheme {
                GameScreen(
                    state = GameState(mode = Mode.RACE, question = question, correct = 7, millisLeft = 8_500),
                    input = InputMode.CHOICE,
                    animationsOn = false,
                    onQuit = {}, onAnswer = {}, onKey = {}, onNext = {},
                )
            }
        }
        compose.onNodeWithText("Dobrze: 7").assertIsDisplayed()
        compose.onNodeWithText("9 s").assertIsDisplayed()
        compose.onNodeWithText("Dalej").assertDoesNotExist()
    }

    @Test
    fun `the result screen reports stars, the summary and the facts to review`() {
        compose.setContent {
            SquaredBoardTheme {
                ResultScreen(
                    state = ResultState(
                        mode = Mode.ADVENTURE,
                        stars = 2,
                        correct = 8,
                        bestRace = 0,
                        newRecord = false,
                        wrong = listOf("7 · 8 = 56", "9 · 6 = 54"),
                        confetti = false,
                    ),
                    animationsOn = false,
                    onAgain = {},
                    onMenu = {},
                )
            }
        }
        compose.onNodeWithText("Bardzo dobrze!").assertIsDisplayed()
        compose.onNodeWithText("8 z 10 dobrze. Zdobyte gwiazdki: 2.").assertIsDisplayed()
        compose.onNodeWithText("Do powtórki").assertIsDisplayed()
        compose.onNodeWithText("7 · 8 = 56").assertIsDisplayed()
        compose.onNodeWithContentDescription("2 gwiazdki na 3").assertIsDisplayed()
        compose.onNodeWithText("Zagraj jeszcze raz").assertIsDisplayed()
    }

    @Test
    fun `a race record is announced instead of the star title`() {
        compose.setContent {
            SquaredBoardTheme {
                ResultScreen(
                    state = ResultState(
                        mode = Mode.RACE,
                        stars = 3,
                        correct = 27,
                        bestRace = 27,
                        newRecord = true,
                        wrong = emptyList(),
                        confetti = false,
                    ),
                    animationsOn = false,
                    onAgain = {},
                    onMenu = {},
                )
            }
        }
        compose.onNodeWithText("Nowy rekord!").assertIsDisplayed()
        compose.onNodeWithText("27 poprawnych odpowiedzi w 60 sekund. Rekord: 27.").assertIsDisplayed()
        compose.onNodeWithText("Do powtórki").assertDoesNotExist()
    }

    @Test
    fun `the board renders every fact and describes the selected one`() {
        var selected: Pair<Int, Int>? = 6 to 7
        compose.setContent {
            SquaredBoardTheme {
                BoardScreen(
                    progress = progress(6 to 7),
                    selected = selected,
                    resetArmed = false,
                    onBack = {},
                    onSelect = { r, c -> selected = r to c },
                    onReset = {},
                )
            }
        }
        compose.onNodeWithText("Moja tabliczka").assertIsDisplayed()
        compose.onNodeWithText("Umiem").assertIsDisplayed()
        compose.onNodeWithText("6 · 7 = 42, 42 : 7 = 6").assertIsDisplayed()
        compose.onNodeWithText("Umiesz to świetnie!").assertIsDisplayed()
        compose.onNodeWithContentDescription("9 razy 9 równa się 81").assertIsDisplayed()
        compose.onNodeWithText("Wyzeruj postępy").assertIsDisplayed()
    }

    @Test
    fun `the reset button asks for a second tap before wiping progress`() {
        compose.setContent {
            SquaredBoardTheme {
                BoardScreen(
                    progress = progress(),
                    selected = null,
                    resetArmed = true,
                    onBack = {}, onSelect = { _, _ -> }, onReset = {},
                )
            }
        }
        compose.onNodeWithText("Na pewno? Dotknij jeszcze raz").assertIsDisplayed()
        compose.onNodeWithText("Dotknij kratkę, żeby zobaczyć działanie.").assertIsDisplayed()
    }
}
