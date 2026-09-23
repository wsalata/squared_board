# Times Tables Grid

*[Wersja polska](README.md)*

An Android app for learning multiplication and division up to 100. The interface looks like a squared exercise book — paper, navy ink and hand-drawn buttons — and is aimed at children in their first school years. Available in Polish and English.

The project is a port of a web prototype to native Android; the code still carries comments that refer back to the original (`prefers-reduced-motion`, `max-width: 480px`, the CSS gradients that drew the grid).

## What it does

**Two game modes**

- **Play** — a round of 10 questions, ending with 0 to 3 stars.
- **Race the clock** — 60 seconds, counting correct answers; the app remembers the best score.

**Four kinds of facts** (`OpMode`)

| Mode | Example | What is hidden |
| --- | --- | --- |
| Multiplication | `3 · 4 = ?` | the product |
| Division | `12 : 4 = ?` | the factor |
| Mixed | randomly one of the above | — |
| Puzzles | `? · 4 = 12` | any part of the equation |

**Two ways to answer** — pick one of four tiles, or type the result on a number pad.

**Choice of tables** — one to ten, toggled on the home screen (at least one must stay on).

**My board** — a 10 × 10 grid showing how well each fact is known, by tile colour. Progress can also be reset from here (it takes two taps).

**Sound and haptics** — `Sfx` synthesises short tones straight into an `AudioTrack` — a sine or triangle wave with a fast attack and exponential decay — so the APK contains no audio files at all.

## How the learning works

The question generator (`QuestionGenerator`) does not draw facts uniformly:

- **Weight follows mastery** — `1 + (5 - score) · 0.5`, so a fact scored 0 comes up about 3.5× more often than one scored 5.
- **A buffer of the last four** — a fact just asked is multiplied by 0.04, so the same question does not come back twice in a row.
- **The trivial ones row quieted down** — multiplying by 1 gets a 0.35 multiplier, unless the player picked the 1 table alone.
- **Believable distractors** — the three wrong answers are neighbouring multiples (`a·(b+1)`, `(a-1)·b`) and off-by-one slips, not random numbers.

Progress (`Progress`) keeps a 0–5 score per fact: a correct answer gives +1, a wrong one −2. From 4 up the fact counts as mastered. The key is symmetric (`tileKey`), so `3 × 4` and `4 × 3` are one entry — both tiles light up on the board at once.

## Languages

English is the default language (`res/values/`) and Polish lives in `res/values-pl/`, so a phone set to any third language gets English rather than Polish. No user-facing text is hardcoded.

Plural agreement is handled by `<plurals>` resources, which means the CLDR rules built into Android. Polish gets the `one`/`few`/`many` forms including the exception for the teens ("1 poprawna odpowiedź", "22 poprawne odpowiedzi", "14 poprawnych odpowiedzi"); English gets `one`/`other`.

Text decided outside composition — the praise and feedback after an answer, the result screen title — travels as `UiText`, a resource id with its arguments, so `AppViewModel` needs no `Context`. It is resolved inside a composable by `UiText.resolve()`.

## Tech stack

- **Kotlin 2.3.21**, **Jetpack Compose** (BOM 2026.09.00), Material 3
- **AGP 9.4.1**, Gradle with a version catalog (`gradle/libs.versions.toml`) and the configuration cache
- **DataStore Preferences** — persists progress and settings
- **Robolectric 4.17** — Compose tests on the JVM, no emulator
- minSdk 28, target/compileSdk 37, Java 17 toolchain and source compatibility
- Baloo 2 as a single variable font (axes instead of one file per weight)

Every graphic — icons, confetti, the sketched button outlines, the grid in the background — is drawn in `Canvas`/`drawBehind`. There are no bitmaps in the resources beyond the launcher icon.

## Project layout

```
app/src/main/java/eu/tudek/squared_board/
├── MainActivity.kt            entry point, edge-to-edge, reads the system animation setting
├── data/
│   ├── Progress.kt            progress and settings model, mastery logic
│   └── ProgressStore.kt       serialisation to DataStore
├── game/
│   ├── AppViewModel.kt        app state, round flow, race timer
│   ├── GameState.kt           state of a round and of the result screen
│   ├── Questions.kt           question and distractor generator
│   └── UiText.kt              text as a resource id, resolved in the UI
├── sound/Sfx.kt               AudioTrack sound synthesis + haptics
└── ui/
    ├── SquaredBoardApp.kt     navigation between screens, Back handling
    ├── HomeScreen.kt          settings and mode choice
    ├── GameScreen.kt          equation, answers, keypad, question counter / clock
    ├── ResultScreen.kt        stars, summary, facts to review
    ├── BoardScreen.kt         the 10 × 10 board
    ├── components/            Sketch, Paper, Confetti, Icons, ScaleToFit, Modifiers
    └── theme/Theme.kt         the Ink palette and text styles
```

## Building

A JDK has to be on `PATH` — Gradle starts its daemon on it before it even reads the build files. The JDK that compiles the code downloads itself (toolchain 17 plus `foojay-resolver`), so any reasonably recent Java will do:

```fish
sudo pacman -S jdk-openjdk        # or: set -Ux JAVA_HOME ~/android-studio/jbr
```

Then:

```fish
./gradlew assembleDebug      # build the APK
./gradlew installDebug       # install on a connected device
./gradlew clean              # clean the build directory
```

The path to the Android SDK goes into `local.properties`, which is not versioned. Android Studio generates it when the project is first opened.

## Releases

`bundleRelease` produces the App Bundle that Google Play expects, with R8 enabled:

```fish
./gradlew bundleRelease
```

Signing is driven by environment variables (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`), falling back to Gradle properties (`SB_STORE_FILE` and friends) in `~/.gradle/gradle.properties`. With neither present the release build still works and comes out unsigned.

`.github/workflows/release.yml` runs the same build on a `v*` tag: unit tests, a signed AAB and APK from repository secrets, an `apksigner` check, and the bundle attached to the GitHub Release together with `mapping.txt`. Keep that mapping file — without it an R8 stack trace is unreadable.

## Tests

31 unit tests, all running on the JVM:

```fish
./gradlew testDebugUnitTest
```

| File | Scope |
| --- | --- |
| `QuestionGeneratorTest` | equation shape in every mode, table selection, distractors, weighting of weak facts |
| `ProgressTest` | scoring, key symmetry, mastery threshold |
| `LocalizationTest` | the English default resources and English plural forms |
| `ScreenRenderTest` | rendering every screen through Robolectric (under the `pl` qualifier) |
| `AppFlowTest` | screen transitions, changing settings, starting a race |

Robolectric renders the Compose screens on the JVM, so no emulator is needed — that is what `testOptions { unitTests { isIncludeAndroidResources = true } }` in `app/build.gradle.kts` enables.

The HTML report lands in `app/build/reports/tests/testDebugUnitTest/index.html`.

## Accessibility

- The app honours the system animation setting (`ValueAnimator.areAnimatorsEnabled()`) — with animations off there is no confetti and no card shake.
- Equations carry a spoken screen-reader description (`Question.spoken()`): "3 times 4 equals what". The words come from resources, so the screen reader speaks the phone's language.
- Board tiles and table buttons have a `contentDescription`.
- `ScaleToFit` shrinks the equation on narrow screens instead of clipping it.
- Text colour on board tiles follows the tile's brightness (`Ink.onTier`).
