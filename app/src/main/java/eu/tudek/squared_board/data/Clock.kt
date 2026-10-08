package eu.tudek.squared_board.data

import java.time.LocalDate

/**
 * "Which day is it?" as a single number, so the daily streak can be reasoned about — and
 * tested — without a real calendar. Epoch day: 1970-01-01 is 0.
 *
 * The day, not the time: a child's streak follows the calendar on their own device, so the
 * device's time zone is exactly the right one to read it in.
 */
fun interface Clock {
    fun today(): Int
}

object SystemClock : Clock {
    override fun today(): Int = LocalDate.now().toEpochDay().toInt()
}
