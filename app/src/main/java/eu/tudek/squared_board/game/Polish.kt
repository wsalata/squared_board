package eu.tudek.squared_board.game

/**
 * Polish has three plural forms: one for 1, one for 2..4, and one for everything else,
 * with the teens falling back to the "many" form.
 */
fun plural(n: Int, one: String, few: String, many: String): String {
    if (n == 1) return one
    val d = n % 10
    val h = n % 100
    return if (d in 2..4 && h !in 12..14) few else many
}
