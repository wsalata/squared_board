package eu.tudek.squared_board

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import eu.tudek.squared_board.ui.SquaredBoardApp
import eu.tudek.squared_board.ui.theme.Ink
import eu.tudek.squared_board.ui.theme.SquaredBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // API 35+ is always edge-to-edge for this targetSdk; older versions need the opt-in.
        // Bar colours come from the theme (see themes.xml for why not enableEdgeToEdge()).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
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
