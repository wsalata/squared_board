package eu.tudek.squared_board

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import eu.tudek.squared_board.ui.SquaredBoardApp
import eu.tudek.squared_board.ui.theme.Ink
import eu.tudek.squared_board.ui.theme.SquaredBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SquaredBoardTheme {
                Surface(color = Ink.paper) {
                    SquaredBoardApp(animationsOn = animationsEnabled())
                }
            }
        }
    }

    /**
     * Honours "remove animations" in the system settings, the way the prototype honoured
     * `prefers-reduced-motion`.
     */
    private fun animationsEnabled(): Boolean = ValueAnimator.areAnimatorsEnabled()
}
