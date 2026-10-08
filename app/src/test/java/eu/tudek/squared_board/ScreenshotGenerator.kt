package eu.tudek.squared_board

import android.graphics.Bitmap
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asAndroidBitmap
import eu.tudek.squared_board.data.InputMode
import eu.tudek.squared_board.data.OpMode
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.Settings
import eu.tudek.squared_board.data.StickerId
import eu.tudek.squared_board.game.AnswerKind
import eu.tudek.squared_board.game.DailyState
import eu.tudek.squared_board.game.GameState
import eu.tudek.squared_board.game.Mode
import eu.tudek.squared_board.game.Question
import eu.tudek.squared_board.game.ResultState
import eu.tudek.squared_board.game.Token
import eu.tudek.squared_board.game.UiText
import eu.tudek.squared_board.ui.BoardScreen
import eu.tudek.squared_board.ui.GameScreen
import eu.tudek.squared_board.ui.HomeScreen
import eu.tudek.squared_board.ui.ResultScreen
import eu.tudek.squared_board.ui.StickersScreen
import eu.tudek.squared_board.ui.components.paperGrid
import eu.tudek.squared_board.ui.theme.SquaredBoardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Nie jest to test, tylko generator zrzutów ekranu do listingu w sklepie.
 * Renderuje ekrany tak, jak składa je SquaredBoardApp, i zapisuje PNG do play/screenshots/.
 *
 * Uruchomienie:  ./gradlew testDebugUnitTest --tests '*ScreenshotGenerator*'
 *
 * w360dp-h640dp-xxhdpi daje 1080 x 1920 px, czyli proporcje 9:16 wymagane przez Google Play.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "pl-rPL-w360dp-h640dp-xxhdpi")
open class ScreenshotGenerator {

    @get:Rule
    val compose = createComposeRule()

    protected open val outDir: File get() = File("../play/screenshots").apply { mkdirs() }

    private fun progress(vararg learned: Pair<Int, Int>, settings: Settings = Settings()): Progress {
        var p = Progress(settings = settings)
        learned.forEach { (a, b) -> repeat(5) { p = p.withAnswer(a, b, correct = true) } }
        return p
    }

    private val question = Question(
        a = 3, b = 4,
        tokens = listOf(Token.Num(3), Token.Sym("·"), Token.Num(4), Token.Sym("="), Token.Blank),
        ans = 12, kind = AnswerKind.PRODUCT,
    )

    /** Ta sama otoczka co w SquaredBoardApp: kratkowane tło i wyśrodkowana kolumna. */
    private fun shot(name: String, scrollPx: Int = 0, content: @Composable () -> Unit) {
        compose.setContent {
            SquaredBoardTheme {
                Box(Modifier.fillMaxSize().paperGrid()) {
                    Column(
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState(initial = scrollPx))
                            .padding(horizontal = 16.dp)
                            .padding(top = 14.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.Top,
                    ) { content() }
                }
            }
        }
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val file = File(outDir, "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("ZRZUT: ${file.absolutePath} (${bitmap.width} x ${bitmap.height})")
    }

    @Test
    fun `1 ekran glowny`() = shot("1-ekran-glowny") {
        HomeScreen(
            progress = progress(3 to 4, 6 to 7, 2 to 8, 5 to 5, 9 to 3),
            daily = DailyState(streak = 4, days = listOf(false, false, true, true, true, true, false), firstDay = 20_000),
            animationsOn = false,
            onToggleSound = {}, onOpenBoard = {}, onOpenStickers = {}, onPlayDaily = {},
            onOp = {}, onToggleTable = {},
            onSelectAll = {}, onInput = {}, onPlay = {},
        )
    }

    @Test
    fun `7 tryby gry`() = shot("7-tryby", scrollPx = 1150) {
        HomeScreen(
            progress = progress(3 to 4, 6 to 7, 2 to 8, 5 to 5, 9 to 3),
            daily = DailyState(streak = 4, days = listOf(false, false, true, true, true, true, false), firstDay = 20_000),
            animationsOn = false,
            onToggleSound = {}, onOpenBoard = {}, onOpenStickers = {}, onPlayDaily = {},
            onOp = {}, onToggleTable = {},
            onSelectAll = {}, onInput = {}, onPlay = {},
        )
    }

    @Test
    fun `2 gra wybor odpowiedzi`() = shot("2-gra") {
        GameScreen(
            state = GameState(
                mode = Mode.ADVENTURE, question = question,
                choices = listOf(12, 15, 9, 16), correct = 3, shown = 4, index = 3,
            ),
            input = InputMode.CHOICE, animationsOn = false,
            onQuit = {}, onAnswer = {}, onKey = {}, onNext = {},
        )
    }

    @Test
    fun `3 dobra odpowiedz`() = shot("3-dobra-odpowiedz") {
        GameScreen(
            state = GameState(
                mode = Mode.ADVENTURE, question = question,
                choices = listOf(12, 15, 9, 16), correct = 4, shown = 4, index = 3,
                locked = true, chosen = 12, feedbackOk = true,
                feedback = UiText.Res(R.string.praise_4),
            ),
            input = InputMode.CHOICE, animationsOn = false,
            onQuit = {}, onAnswer = {}, onKey = {}, onNext = {},
        )
    }

    @Test
    fun `4 wynik rundy`() = shot("4-wynik") {
        ResultScreen(
            state = ResultState(
                mode = Mode.ADVENTURE, stars = 3, correct = 10, bestRace = 0,
                newRecord = false, wrong = emptyList(), confetti = false,
            ),
            animationsOn = false, onAgain = {}, onMenu = {},
        )
    }

    @Test
    fun `5 moja tabliczka`() = shot("5-tabliczka") {
        val learned = buildList {
            for (t in 1..10) for (x in 1..10) if ((t * x) % 3 == 0 || t <= 3) add(t to x)
        }.take(46).toTypedArray()
        BoardScreen(
            progress = progress(*learned),
            selected = 7 to 8,
            resetArmed = false,
            onBack = {}, onSelect = { _, _ -> }, onReset = {},
        )
    }

    @Test
    fun `6 klawiatura`() = shot("6-klawiatura") {
        GameScreen(
            state = GameState(
                mode = Mode.RACE, question = question, typed = "1",
                correct = 11, millisLeft = 23_000,
            ),
            input = InputMode.KEYPAD, animationsOn = false,
            onQuit = {}, onAnswer = {}, onKey = {}, onNext = {},
        )
    }

    @Test
    fun `8 naklejki`() = shot("8-naklejki") {
        StickersScreen(
            progress = progress().copy(
                stickers = setOf(
                    StickerId.TABLE_1, StickerId.TABLE_2, StickerId.TABLE_3, StickerId.TABLE_4,
                    StickerId.TABLE_5, StickerId.TABLE_6, StickerId.TABLE_7,
                    StickerId.STARS_10, StickerId.RACE_20, StickerId.STREAK_3,
                ),
            ),
            selected = StickerId.TABLE_7,
            onBack = {},
            onSelect = {},
        )
    }

    /** Every drawing at once: the quickest way to eyeball the path data after a change. */
    @Test
    fun `9 wszystkie naklejki`() = shot("9-wszystkie-naklejki") {
        StickersScreen(
            progress = progress().copy(stickers = StickerId.entries.toSet()),
            selected = null,
            onBack = {},
            onSelect = {},
        )
    }

    @Test
    fun `10 nowa naklejka`() = shot("10-nowa-naklejka") {
        ResultScreen(
            state = ResultState(
                mode = Mode.DAILY,
                total = 5,
                stars = 3,
                bonusStar = true,
                correct = 5,
                bestRace = 0,
                newRecord = false,
                wrong = emptyList(),
                newStickers = listOf(StickerId.TABLE_7),
                streak = 4,
                confetti = false,
            ),
            animationsOn = false,
            onAgain = {},
            onMenu = {},
        )
    }

    @Suppress("unused")
    private val unusedOpMode = OpMode.MUL
}

/** Ten sam komplet zrzutów, tyle że po angielsku — do listingu en-US. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "en-rUS-w360dp-h640dp-xxhdpi")
class ScreenshotGeneratorEn : ScreenshotGenerator() {
    override val outDir: File get() = File("../play/screenshots-en").apply { mkdirs() }

}
