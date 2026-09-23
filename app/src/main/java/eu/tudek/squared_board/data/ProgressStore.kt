package eu.tudek.squared_board.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tabliczka-w-kratke-v1")

/** Persists [Progress], standing in for the prototype's browser storage. */
class ProgressStore(context: Context) {

    private val store = context.dataStore

    val progress: Flow<Progress> = store.data.map { it.toProgress() }

    suspend fun save(p: Progress) {
        store.edit { prefs ->
            prefs[KEY_TILES] = p.tiles.entries
                .filter { it.value > 0 }
                .joinToString(",") { "${it.key}:${it.value}" }
            prefs[KEY_STARS] = p.stars
            prefs[KEY_BEST_RACE] = p.bestRace
            prefs[KEY_OP] = p.settings.op.name
            prefs[KEY_TABLES] = p.settings.tables.joinToString(",")
            prefs[KEY_INPUT] = p.settings.input.name
            prefs[KEY_SOUND] = p.settings.sound
        }
    }

    private fun Preferences.toProgress(): Progress {
        val defaults = Settings()
        val tiles = (this[KEY_TILES] ?: "")
            .split(",")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                val level = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                if (parts[0].isEmpty()) null else parts[0] to level.coerceIn(0, 5)
            }
            .toMap()
        val tables = (this[KEY_TABLES] ?: "")
            .split(",")
            .mapNotNull { it.toIntOrNull() }
            .filter { it in 1..10 }
            .sorted()
        return Progress(
            tiles = tiles,
            stars = this[KEY_STARS] ?: 0,
            bestRace = this[KEY_BEST_RACE] ?: 0,
            settings = Settings(
                op = this[KEY_OP]?.let { name -> OpMode.entries.firstOrNull { it.name == name } } ?: defaults.op,
                tables = tables.ifEmpty { defaults.tables },
                input = this[KEY_INPUT]?.let { name -> InputMode.entries.firstOrNull { it.name == name } } ?: defaults.input,
                sound = this[KEY_SOUND] ?: defaults.sound,
            ),
        )
    }

    private companion object {
        val KEY_TILES = stringPreferencesKey("tiles")
        val KEY_STARS = intPreferencesKey("stars")
        val KEY_BEST_RACE = intPreferencesKey("bestRace")
        val KEY_OP = stringPreferencesKey("op")
        val KEY_TABLES = stringPreferencesKey("tables")
        val KEY_INPUT = stringPreferencesKey("input")
        val KEY_SOUND = booleanPreferencesKey("sound")
    }
}
