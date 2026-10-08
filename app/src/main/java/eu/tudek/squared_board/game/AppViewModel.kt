package eu.tudek.squared_board.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eu.tudek.squared_board.R
import eu.tudek.squared_board.data.Clock
import eu.tudek.squared_board.data.InputMode
import eu.tudek.squared_board.data.OpMode
import eu.tudek.squared_board.data.Progress
import eu.tudek.squared_board.data.ProgressStore
import eu.tudek.squared_board.data.Settings
import eu.tudek.squared_board.data.StickerId
import eu.tudek.squared_board.data.SystemClock
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

private val PRAISE = listOf(
    R.string.praise_1, R.string.praise_2, R.string.praise_3, R.string.praise_4,
    R.string.praise_5, R.string.praise_6, R.string.praise_7,
)

/** How much harder the challenge of the day leans on the facts the player is weakest at. */
private const val DAILY_WEAK_BIAS = 3.0

class AppViewModel(app: Application, private val clock: Clock) : AndroidViewModel(app) {

    /** The constructor AndroidViewModelFactory finds by reflection. */
    constructor(app: Application) : this(app, SystemClock)

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

    /** The streak strip and the challenge card: progress read against today's date. */
    private val _daily = MutableStateFlow(DailyState.EMPTY)
    val daily: StateFlow<DailyState> = _daily.asStateFlow()

    /** Which tile the board screen has selected, as a 1..10 row/column pair. */
    private val _selectedTile = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedTile: StateFlow<Pair<Int, Int>?> = _selectedTile.asStateFlow()

    /** Which sticker the album has open, so its unlock rule can be spelled out underneath. */
    private val _selectedSticker = MutableStateFlow<StickerId?>(null)
    val selectedSticker: StateFlow<StickerId?> = _selectedSticker.asStateFlow()

    /** True once the player has armed the progress reset and we are waiting for the confirming tap. */
    private val _resetArmed = MutableStateFlow(false)
    val resetArmed: StateFlow<Boolean> = _resetArmed.asStateFlow()

    private var generator: QuestionGenerator? = null
    private var timerJob: Job? = null
    private var advanceJob: Job? = null

    /** The album as this round found it, so a table finished mid-round is still revealed at the end. */
    private var stickersAtRoundStart: Set<StickerId> = emptySet()

    /** Set by the UI so shake and confetti honour the system animation setting. */
    var animationsOn: Boolean = true

    init {
        viewModelScope.launch {
            val loaded = store.progress.first()
            // Someone who already filled their board has earned most of the album: bank it
            // quietly on this first load rather than avalanching reveals onto their next round.
            val (backfilled, added) = loaded.awardStickers()
            _progress.value = backfilled
            sfx.enabled = backfilled.settings.sound
            refreshDaily()
            if (added.isNotEmpty()) persist()
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
        refreshDaily()
        viewModelScope.launch { store.save(snapshot) }
    }

    /** Recomputed rather than mapped from [progress], because it depends on the date too. */
    private fun refreshDaily() {
        _daily.value = DailyState.from(_progress.value, clock.today())
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

    fun openStickers() {
        sfx.tap()
        _selectedSticker.value = null
        _screen.value = Screen.STICKERS
    }

    fun selectSticker(id: StickerId) {
        sfx.tap()
        _selectedSticker.value = if (_selectedSticker.value == id) null else id
    }

    fun goHome() {
        stopGame()
        // Landing on the menu is the moment the strip is looked at, so re-read the date here
        // as well: an app left open past midnight would otherwise show yesterday's row.
        refreshDaily()
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
        _selectedSticker.value = null
        // The album goes too: with a blank board it would be claiming things that are no
        // longer true. This is already behind a confirming second tap.
        _progress.value = Progress(settings = _progress.value.settings)
        persist()
    }

    // ---------------- game flow ----------------

    fun startGame(mode: Mode) {
        advanceJob?.cancel()
        timerJob?.cancel()
        stickersAtRoundStart = _progress.value.stickers
        val daily = mode == Mode.DAILY
        val gen = QuestionGenerator(
            settings = _progress.value.settings,
            progress = { _progress.value },
            random = { Random.nextDouble() },
            weakBias = if (daily) DAILY_WEAK_BIAS else 1.0,
        )
        generator = gen
        val q = gen.next()
        _game.value = GameState(
            mode = mode,
            question = q,
            total = if (daily) DAILY_ROUND else ROUND,
            choices = if (_progress.value.settings.input == InputMode.CHOICE) gen.choices(q) else emptyList(),
        )
        _screen.value = Screen.GAME
        if (mode == Mode.RACE) startTimer()
    }

    /** The challenge of the day. Tapping it again once it is done does nothing. */
    fun startDaily() {
        if (_progress.value.challengeDone(clock.today())) return
        startGame(Mode.DAILY)
    }

    /** "Play again" never re-enters a challenge that has just been used up. */
    fun replay() = startGame(_result.value?.mode?.takeIf { it != Mode.DAILY } ?: Mode.ADVENTURE)

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
        refreshDaily()
        _screen.value = Screen.HOME
    }

    fun nextQuestion() {
        val g = _game.value ?: return
        val gen = generator ?: return
        advanceJob?.cancel()
        if (g.finished) return
        if (g.mode != Mode.RACE && g.index >= g.total) {
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
            feedback = null,
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

        // Banked as we go, so quitting halfway through never costs an unlock; the reveal
        // still waits for the result screen.
        _progress.value = _progress.value.withAnswer(q.a, q.b, ok).awardStickers().first

        val streak = if (ok) g.streak + 1 else 0
        val feedback: UiText = if (ok) {
            if (streak >= 5 && streak % 5 == 0) {
                UiText.Res(R.string.feedback_streak, listOf(streak))
            } else {
                UiText.Res(PRAISE.random())
            }
        } else if (_progress.value.settings.input == InputMode.KEYPAD) {
            UiText.Res(R.string.feedback_typed, listOf(value, q.fullText()))
        } else {
            UiText.Res(R.string.feedback_close, listOf(q.fullText()))
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

        if (g.mode != Mode.RACE) {
            next = next.copy(
                results = next.results.toMutableList().also { it[g.index] = ok },
                index = g.index + 1,
                // A right answer flows on by itself; a wrong one waits, so the fact can sink in.
                showNext = !ok,
                nextLabel = if (g.index + 1 >= g.total) R.string.game_see_result else R.string.game_next,
            )
        }
        _game.value = next

        if (ok) sfx.good() else sfx.bad()
        persist()

        if (g.mode != Mode.RACE) {
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
            // The same thresholds the ten-question round has always used, as fractions, so a
            // five-question challenge asks for a sensible four and three.
            when {
                g.correct == g.total -> 3
                g.correct >= g.total * 0.8 -> 2
                g.correct >= g.total * 0.5 -> 1
                else -> 0
            }
        }
        val today = clock.today()
        val newRecord = race && g.correct > _progress.value.bestRace
        val played = g.correct > 0
        val bonusStar = g.mode == Mode.DAILY && played && !_progress.value.challengeDone(today)

        var p = _progress.value.copy(
            stars = _progress.value.stars + stars + if (bonusStar) 1 else 0,
            bestRace = if (newRecord) g.correct else _progress.value.bestRace,
        )
        // Any finished round with something right counts as "I practised today" — a child who
        // plays plenty but skips the challenge card must not lose their streak over it.
        if (played) p = p.withDayPlayed(today)
        if (bonusStar) p = p.copy(dailyDoneDay = today)
        // Stars are credited first, so a round that pushes the counter past a threshold
        // unlocks that sticker on this very result screen.
        val awarded = p.awardStickers().first
        _progress.value = awarded
        persist()

        val revealed = (awarded.stickers - stickersAtRoundStart).toList()
        _result.value = ResultState(
            mode = g.mode,
            total = g.total,
            stars = stars,
            bonusStar = bonusStar,
            correct = g.correct,
            bestRace = awarded.bestRace,
            newRecord = newRecord,
            wrong = g.wrong.take(8).map { it.fullText() },
            newStickers = revealed,
            streak = awarded.streakOn(today),
            confetti = (stars == 3 || revealed.isNotEmpty()) && animationsOn,
        )
        _screen.value = Screen.RESULT
        if (revealed.isNotEmpty()) sfx.sticker() else if (stars >= 2 || newRecord) sfx.win()
    }

    override fun onCleared() {
        sfx.release()
    }
}
