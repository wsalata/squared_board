package eu.tudek.squared_board.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

private const val SAMPLE_RATE = 22050

private enum class Wave { SINE, TRIANGLE }

/**
 * The prototype's beeps, rebuilt on AudioTrack: each call renders a short sequence of
 * enveloped tones into one PCM buffer and plays it off the main thread.
 */
class Sfx(context: Context) {

    private val player = Executors.newSingleThreadExecutor()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    /** Mirrors the prototype's sound toggle; kept here so callers need not pass it every time. */
    @Volatile
    var enabled: Boolean = true

    fun good() = tone(listOf(660.0, 990.0), 0.09)

    fun bad() {
        tone(listOf(294.0, 220.0), 0.14, Wave.TRIANGLE, 0.2)
        vibrate(90)
    }

    fun win() = tone(listOf(523.0, 659.0, 784.0, 1047.0), 0.12)

    fun tap() = tone(listOf(520.0), 0.04, vol = 0.06)

    fun release() = player.shutdown()

    private fun vibrate(ms: Long) {
        if (!enabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        runCatching { v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)) }
    }

    private fun tone(freqs: List<Double>, dur: Double, wave: Wave = Wave.SINE, vol: Double = 0.16) {
        if (!enabled) return
        val perTone = (SAMPLE_RATE * dur).roundToInt()
        if (perTone <= 0) return
        val samples = ShortArray(perTone * freqs.size)
        freqs.forEachIndexed { index, freq ->
            val step = 2 * PI * freq / SAMPLE_RATE
            for (i in 0 until perTone) {
                val t = i.toDouble() / perTone
                val phase = step * i
                val raw = when (wave) {
                    Wave.SINE -> sin(phase)
                    // A triangle is a folded sawtooth; it gives the "wrong answer" buzz its edge.
                    Wave.TRIANGLE -> 1 - 4 * abs((phase / (2 * PI) % 1.0) - 0.5)
                }
                samples[index * perTone + i] = (raw * vol * envelope(t) * Short.MAX_VALUE).toInt().toShort()
            }
        }
        play(samples)
    }

    /** Quick attack, exponential decay — the shape the Web Audio gain ramps produced. */
    private fun envelope(t: Double): Double {
        val attack = 0.12
        return if (t < attack) t / attack else exp(-6.0 * (t - attack) / (1 - attack))
    }

    private fun play(samples: ShortArray) {
        player.execute {
            runCatching {
                val bytes = samples.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(bytes)
                    .build()
                track.write(samples, 0, samples.size)
                track.setVolume(AudioTrack.getMaxVolume())
                track.play()
                // MODE_STATIC needs the track kept alive until the buffer has been heard.
                Thread.sleep((samples.size * 1000L / SAMPLE_RATE) + 120)
                track.stop()
                track.release()
            }
        }
    }
}
