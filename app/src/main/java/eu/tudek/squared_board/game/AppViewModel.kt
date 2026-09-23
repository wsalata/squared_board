package eu.tudek.squared_board.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eu.tudek.squared_board.data.InputMode
import eu.tudek.squared_board.data.OpMode
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.ProgressStore
import eu.tudek.squared_board.data.Settings
import eu.tudek.squared_board.sound.Sfx
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

private val PRAISE = listOf("Brawo!", "Świetnie!", "Super!", "Dokładnie tak!", "Tak trzymaj!", "Pięknie!", "Ekstra!")

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val store = ProgressStore(app)
    val sfx = Sfx(app)

    private val _progress = MutableStateFlow(Progress())
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    private val _screen = MutableStateFlow(Screen.HOME)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _game = MutableStateFlow<GameState?>(null)
    val game: StateFlow<GameState?> = _game.asStateFlow()

    private val _result = MutableStateFlow<ResultState?>(null)
    val result: StateFlow<ResultState?> = _result.asStateFlow()

    /** Which tile the board screen has selected, as a 1..10 row/column pair. */
    private val _selectedTile = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedTile: StateFlow<Pair<Int, Int>?> = _selectedTile.asStateFlow()

    /** True once the player has armed "Wyzeruj postępy" and we are waiting for the confirming tap. */
    private val _resetArmed = MutableStateFlow(false)
    val resetArmed: StateFlow<Boolean> = _resetArmed.asStateFlow()

    private var generator: QuestionGenerator? = null
    private var timerJob: Job? = null
    private var advanceJob: Job? = null

    /** Set by the UI so shake and confetti honour the system animation setting. */
    var animationsOn: Boolean = true

    init {
        viewModelScope.launch {
            _progress.value = store.progress.first()
            sfx.enabled = _progress.value.settings.sound
        }
    }

    // ---------------- settings ----------------

    private fun updateSettings(block: (Settings) -> Settings) {
        val next = _progress.value.copy(settings = block(_progress.value.settings))
        _progress.value = next
        sfx.enabled = next.settings.sound
        persist()
    }

    private fun persist() {
        val snapshot = _progress.value
        viewModelScope.launch { store.save(snapshot) }
    }

    fun setOp(op: OpMode) {
        sfx.tap()
        updateSettings { it.copy(op = op) }
    }

    fun setInput(input: InputMode) {
        sfx.tap()
        updateSettings { it.copy(input = input) }
    }

    /** Tapping a table toggles it, but at least one must stay on. */
    fun toggleTable(n: Int) {
        sfx.tap()
        updateSettings { s ->
            val next = if (n in s.tables) {
                if (s.tables.size > 1) s.tables - n else s.tables
            } else {
                s.tables + n
            }
            s.copy(tables = next.sorted())
        }
    }

    fun selectAllTables() {
        sfx.tap()
        updateSettings { it.copy(tables = (1..10).toList()) }
    }

    fun toggleSound() {
        updateSettings { it.copy(sound = !it.sound) }
        sfx.tap()
    }

    // ---------------- navigation ----------------

    fun openBoard() {
        _resetArmed.value = false
        _screen.value = Screen.BOARD
    }

    fun goHome() {
        stopGame()
        _screen.value = Screen.HOME
    }

    fun selectTile(row: Int, col: Int) {
        sfx.tap()
        _selectedTile.value = row to col
    }

    /** The first tap arms the reset, the second one performs it — settings are kept. */
    fun resetProgress() {
        if (!_resetArmed.value) {
            _resetArmed.value = true
            return
        }
        _resetArmed.value = false
        _selectedTile.value = null
        _progress.value = Progress(settings = _progress.value.settings)
        persist()
    }

    // ---------------- game flow ----------------

    fun startGame(mode: Mode) {
        advanceJob?.cancel()
        timerJob?.cancel()
        val gen = QuestionGenerator(_progress.value.settings, { _progress.value }) { Random.nextDouble() }
        generator = gen
        val q = gen.next()
        _game.value = GameState(
            mode = mode,
            question = q,
            choices = if (_progress.value.settings.input == InputMode.CHOICE) gen.choices(q) else emptyList(),
        )
        _screen.value = Screen.GAME
        if (mode == Mode.RACE) startTimer()
    }

    fun replay() = startGame(_result.value?.mode ?: Mode.ADVENTURE)

    private fun startTimer() {
        val endAt = System.nanoTime() + RACE_MS * 1_000_000
        timerJob = viewModelScope.launch {
            var expired = false
            while (isActive && !expired) {
                val left = ((endAt - System.nanoTime()) / 1_000_000).coerceAtLeast(0)
                _game.value = _game.value?.copy(millisLeft = left) ?: return@launch
                if (left <= 0L) expired = true else delay(100)
            }
            if (expired) finish()
        }
    }

    private fun stopGame() {
        advanceJob?.cancel()
        timerJob?.cancel()
        _game.value = null
    }

    fun quit() {
        stopGame()
        _screen.value = Screen.HOME
    }

    fun nextQuestion() {
        val g = _game.value ?: return
        val gen = generator ?: return
        advanceJob?.cancel()
        if (g.finished) return
        if (g.mode == Mode.ADVENTURE && g.index >= ROUND) {
            finish()
            return
        }
        val q = gen.next()
        _game.value = g.copy(
            question = q,
            choices = if (_progress.value.settings.input == InputMode.CHOICE) gen.choices(q) else emptyList(),
            shown = g.index + 1,
            locked = false,
            typed = "",
            chosen = null,
            feedback = "",
            feedbackOk = null,
            showNext = false,
        )
    }

    // ---------------- answering ----------------

    fun keyPress(key: String) {
        val g = _game.value ?: return
        if (g.locked || _progress.value.settings.input != InputMode.KEYPAD) return
        when (key) {
            "del" -> _game.value = g.copy(typed = g.typed.dropLast(1))
            "ok" -> if (g.typed.isNotEmpty()) {
                submit(g.typed.toInt())
                return
            }
            else -> if (g.typed.length < 3) {
                _game.value = g.copy(typed = (if (g.typed == "0") "" else g.typed) + key)
            }
        }
        sfx.tap()
    }

    fun submit(value: Int) {
        val g = _game.value ?: return
        if (g.locked || g.finished) return
        val q = g.question
        val ok = value == q.ans

        _progress.value = _progress.value.withAnswer(q.a, q.b, ok)

        val streak = if (ok) g.streak + 1 else 0
        val feedback = if (ok) {
            if (streak >= 5 && streak % 5 == 0) "$streak z rzędu! Niesamowite!" else PRAISE.random()
        } else if (_progress.value.settings.input == InputMode.KEYPAD) {
            "Wpisano $value. Zapamiętaj: ${q.fullText()}"
        } else {
            "Prawie! Zapamiętaj: ${q.fullText()}"
        }

        var next = g.copy(
            locked = true,
            chosen = value,
            correct = if (ok) g.correct + 1 else g.correct,
            streak = streak,
            bestStreak = maxOf(g.bestStreak, streak),
            wrong = if (ok || g.wrong.any { it.fullText() == q.fullText() }) g.wrong else g.wrong + q,
            feedback = feedback,
            feedbackOk = ok,
            shakeTick = if (ok) g.shakeTick else g.shakeTick + 1,
        )

        if (g.mode == Mode.ADVENTURE) {
            next = next.copy(
                results = next.results.toMutableList().also { it[g.index] = ok },
                index = g.index + 1,
                // A right answer flows on by itself; a wrong one waits, so the fact can sink in.
                showNext = !ok,
                nextLabel = if (g.index + 1 >= ROUND) "Zobacz wynik" else "Dalej",
            )
        }
        _game.value = next

        if (ok) sfx.good() else sfx.bad()
        persist()

        if (g.mode == Mode.ADVENTURE) {
            if (ok) later(800) { nextQuestion() }
        } else {
            later(if (ok) 450 else 1300) { nextQuestion() }
        }
    }

    /** Runs [block] later, but only while this same round is still going. */
    private fun later(ms: Long, block: () -> Unit) {
        val round = _game.value
        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(ms)
            val current = _game.value
            if (current != null && current.mode == round?.mode && !current.finished) block()
        }
    }

    // ---------------- finishing ----------------

    private fun finish() {
        val g = _game.value ?: return
        if (g.finished) return
        advanceJob?.cancel()
        timerJob?.cancel()
        _game.value = g.copy(finished = true)

        val race = g.mode == Mode.RACE
        val stars = if (race) {
            when {
                g.correct >= 25 -> 3
                g.correct >= 16 -> 2
                g.correct >= 8 -> 1
                else -> 0
            }
        } else {
            when {
                g.correct == ROUND -> 3
                g.correct >= 8 -> 2
                g.correct >= 5 -> 1
                else -> 0
            }
        }
        val newRecord = race && g.correct > _progress.value.bestRace
        _progress.value = _progress.value.copy(
            stars = _progress.value.stars + stars,
            bestRace = if (newRecord) g.correct else _progress.value.bestRace,
        )
        persist()

        _result.value = ResultState(
            mode = g.mode,
            stars = stars,
            correct = g.correct,
            bestRace = _progress.value.bestRace,
            newRecord = newRecord,
            wrong = g.wrong.take(8).map { it.fullText() },
            confetti = stars == 3 && animationsOn,
        )
        _screen.value = Screen.RESULT
        if (stars >= 2 || newRecord) sfx.win()
    }

    override fun onCleared() {
        sfx.release()
    }
}
