package eu.tudek.squared_board.data

/**
 * One slot in the sticker album.
 *
 * These names are written into storage, so they are the one thing here that must never be
 * renamed; what each one looks like and what unlocks it lives elsewhere and may change freely.
 */
enum class StickerId {
    TABLE_1, TABLE_2, TABLE_3, TABLE_4, TABLE_5,
    TABLE_6, TABLE_7, TABLE_8, TABLE_9, TABLE_10,
    STARS_10, STARS_50,
    BOARD_FULL,
    RACE_20,
    STREAK_3, STREAK_7,
}
