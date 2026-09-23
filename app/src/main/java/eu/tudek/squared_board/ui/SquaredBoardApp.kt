package eu.tudek.squared_board.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.tudek.squared_board.game.AppViewModel
import eu.tudek.squared_board.game.Screen
import eu.tudek.squared_board.ui.components.Confetti
import eu.tudek.squared_board.ui.components.paperGrid

@Composable
fun SquaredBoardApp(animationsOn: Boolean, viewModel: AppViewModel = viewModel()) {
    SideEffect { viewModel.animationsOn = animationsOn }

    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val game by viewModel.game.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val selectedTile by viewModel.selectedTile.collectAsStateWithLifecycle()
    val resetArmed by viewModel.resetArmed.collectAsStateWithLifecycle()

    // Back leaves whichever screen you are on, rather than the app.
    BackHandler(enabled = screen != Screen.HOME) { viewModel.goHome() }

    // Reset on every new result so a second three-star round celebrates too.
    var confettiShown by remember { mutableStateOf(false) }
    LaunchedEffect(result) { confettiShown = false }

    Box(Modifier.fillMaxSize().paperGrid()) {
        val scroll = rememberScrollState()
        // A phone-width column centred on larger screens, as the prototype's max-width did.
        // The grid covers the whole window; only the content keeps clear of the system bars.
        // Width is capped and centred the way the prototype's `max-width: 480px` was.
        val insets = WindowInsets.safeDrawing.asPaddingValues()
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .fillMaxHeight()
                .verticalScroll(scroll)
                .padding(insets)
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 24.dp),
        ) {
            when (screen) {
                Screen.HOME -> HomeScreen(
                    progress = progress,
                    onToggleSound = viewModel::toggleSound,
                    onOpenBoard = viewModel::openBoard,
                    onOp = viewModel::setOp,
                    onToggleTable = viewModel::toggleTable,
                    onSelectAll = viewModel::selectAllTables,
                    onInput = viewModel::setInput,
                    onPlay = viewModel::startGame,
                )

                Screen.GAME -> game?.let { state ->
                    GameScreen(
                        state = state,
                        input = progress.settings.input,
                        animationsOn = animationsOn,
                        onQuit = viewModel::quit,
                        onAnswer = viewModel::submit,
                        onKey = viewModel::keyPress,
                        onNext = viewModel::nextQuestion,
                    )
                }

                Screen.RESULT -> result?.let { state ->
                    ResultScreen(
                        state = state,
                        animationsOn = animationsOn,
                        onAgain = viewModel::replay,
                        onMenu = viewModel::goHome,
                    )
                }

                Screen.BOARD -> BoardScreen(
                    progress = progress,
                    selected = selectedTile,
                    resetArmed = resetArmed,
                    onBack = viewModel::goHome,
                    onSelect = viewModel::selectTile,
                    onReset = viewModel::resetProgress,
                )
            }
        }

        // Each new screen starts at the top, matching the prototype's scrollTo(0, 0).
        LaunchedEffect(screen) { scroll.scrollTo(0) }

        if (result?.confetti == true && !confettiShown) {
            key(result) { Confetti(onFinished = { confettiShown = true }) }
        }
    }
}
