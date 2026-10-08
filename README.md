# Tabliczka w kratkę

*[English version](README.en.md)*

Aplikacja na Androida do nauki tabliczki mnożenia i dzielenia do 100. Interfejs wygląda jak zeszyt w kratkę — papier, granatowy atrament i ręcznie kreślone przyciski — z myślą o dzieciach w wieku wczesnoszkolnym. Dostępna po polsku i po angielsku.

Projekt jest portem prototypu webowego na natywny Android; w kodzie znajdziesz komentarze odnoszące się do oryginału (`prefers-reduced-motion`, `max-width: 480px`, gradienty CSS rysujące kratkę).

## Co potrafi

**Dwa tryby gry**

- **Zagraj** — runda 10 pytań, na koniec od 0 do 3 gwiazdek.
- **Wyścig z czasem** — 60 sekund, liczy się liczba poprawnych odpowiedzi; aplikacja pamięta rekord.

**Cztery rodzaje działań** (`OpMode`)

| Tryb | Przykład | Co jest ukryte |
| --- | --- | --- |
| Mnożenie | `3 · 4 = ?` | iloczyn |
| Dzielenie | `12 : 4 = ?` | czynnik |
| Na zmianę | losowo jedno z powyższych | — |
| Zagadki | `? · 4 = 12` | dowolny element równania |

**Dwa sposoby odpowiadania** — wybór z czterech kafelków albo wpisanie wyniku na klawiaturze numerycznej.

**Wybór tabliczek** — od jednej do dziesięciu, przełączane na ekranie głównym (co najmniej jedna musi zostać włączona).

**Moja tabliczka** — plansza 10 × 10 pokazująca opanowanie każdego działania kolorem kafelka. Stąd też można wyzerować postępy (wymaga dwóch dotknięć).

**Naklejki** — szesnaście rysunkowych doodli odblokowywanych za opanowanie całej tabliczki, progi gwiazdek, rekord wyścigu i serię dni. Zdobyte lądują w albumie (`StickersScreen`), niezdobyte czekają jako kreskowane miejsca z podpowiedzią, co je otworzy. Raz zdobyta naklejka zostaje — reguły są monotoniczne, więc gorszy dzień nie odbiera dziecku niczego.

**Codzienna seria i wyzwanie dnia** — rządek siedmiu dni z ptaszkami na ekranie głównym oraz krótka, pięciopytaniowa runda (`Mode.DAILY`) mocniej dociskająca najsłabsze działania, raz dziennie, za dodatkową gwiazdkę. Serię podbija każda ukończona runda, nie tylko wyzwanie.

**Dźwięk i wibracje** — `Sfx` syntezuje krótkie dźwięki bezpośrednio w `AudioTrack` — fala sinusoidalna lub trójkątna z szybkim atakiem i wykładniczym wybrzmieniem — więc w APK nie ma żadnych plików audio.

## Jak działa nauka

Generator pytań (`QuestionGenerator`) nie losuje działań równomiernie:

- **Waga zależna od opanowania** — `1 + (5 - wynik) · 0.5`, więc działanie o wyniku 0 pojawia się ~3,5 raza częściej niż opanowane na 5.
- **Bufor ostatnich czterech** — świeżo zadane działanie dostaje mnożnik 0.04, żeby to samo pytanie nie wracało pod rząd.
- **Wyciszona jedynka** — mnożenie przez 1 dostaje mnożnik 0.35, chyba że gracz wybrał wyłącznie tabliczkę przez 1.
- **Wiarygodne dystraktory** — trzy błędne odpowiedzi to sąsiednie wielokrotności (`a·(b+1)`, `(a-1)·b`) i pomyłki o ±1, a nie losowe liczby.

Dzień liczony jest jako numer epoki (`Clock.today()`), a cała arytmetyka serii siedzi w czystych funkcjach `Progress` — `withDayPlayed`, `streakOn`, `challengeDone` — więc testuje się ją bez zegara i bez ViewModelu. Cofnięty zegar nic nie zabiera, a powtórka tego samego dnia nic nie dodaje.

Postęp (`Progress`) trzyma dla każdego działania wynik 0–5: poprawna odpowiedź daje +1, błędna −2. Od 4 działanie liczy się jako opanowane. Klucz jest symetryczny (`tileKey`), więc `3 × 4` i `4 × 3` to jeden wpis — na planszy zapalają się oba kafelki naraz.

## Języki

Angielski jest językiem domyślnym (`res/values/`), polski leży w `res/values-pl/`, więc telefon ustawiony na jakikolwiek trzeci język dostanie angielski, a nie polski. Żaden tekst nie jest zaszyty w kodzie.

Odmianę przez liczbę obsługują zasoby `<plurals>`, czyli reguły CLDR wbudowane w Androida. Polski dostaje formy `one`/`few`/`many` wraz z wyjątkiem dla nastek („1 poprawna odpowiedź", „22 poprawne odpowiedzi", „14 poprawnych odpowiedzi"), angielski `one`/`other`.

Teksty powstające poza kompozycją — pochwały i komentarze po odpowiedzi, tytuł ekranu wyniku — wędrują przez `UiText` jako identyfikator zasobu z argumentami, więc `AppViewModel` nie potrzebuje `Context`. Rozwijane są dopiero w composable przez `UiText.resolve()`.

## Stos technologiczny

- **Kotlin 2.3.21**, **Jetpack Compose** (BOM 2026.09.00), Material 3
- **AGP 9.4.1**, Gradle z katalogiem wersji (`gradle/libs.versions.toml`) i configuration cache
- **DataStore Preferences** — zapis postępów i ustawień
- **Robolectric 4.17** — testy Compose na JVM, bez emulatora
- minSdk 28, target/compileSdk 37, toolchain i zgodność źródeł z Javą 17
- Czcionka Baloo 2 jako pojedynczy font zmienny (osie zamiast osobnych plików na każdą grubość)

Cała grafika — ikony, konfetti, kreskowane ramki przycisków, kratka w tle — jest rysowana w `Canvas`/`drawBehind`. W zasobach nie ma bitmap poza ikoną launchera.

## Struktura projektu

```
app/src/main/java/eu/tudek/squared_board/
├── MainActivity.kt            punkt wejścia, edge-to-edge, odczyt ustawień animacji systemu
├── data/
│   ├── Clock.kt               dzień jako numer epoki, podmieniany w testach
│   ├── Progress.kt            model postępów i ustawień, opanowanie działań, arytmetyka serii
│   ├── ProgressStore.kt       serializacja do DataStore
│   └── Stickers.kt            identyfikatory naklejek (to one trafiają na dysk)
├── game/
│   ├── AppViewModel.kt        stan aplikacji, przepływ rundy, timer wyścigu, przyznawanie naklejek
│   ├── GameState.kt           stan rundy, ekranu wyniku i paska dni
│   ├── Questions.kt           generator pytań i dystraktorów
│   ├── StickerCatalog.kt      nazwy, podpowiedzi i reguły zdobycia naklejek
│   └── UiText.kt              tekst jako identyfikator zasobu, rozwijany w UI
├── sound/Sfx.kt               synteza dźwięku na AudioTrack + wibracje
└── ui/
    ├── SquaredBoardApp.kt     nawigacja między ekranami, obsługa Wstecz
    ├── HomeScreen.kt          ustawienia, wybór trybu, karta serii
    ├── GameScreen.kt          równanie, odpowiedzi, klawiatura, licznik pytań / zegar
    ├── ResultScreen.kt        gwiazdki, podsumowanie, nowe naklejki, działania do powtórki
    ├── BoardScreen.kt         plansza 10 × 10
    ├── StickersScreen.kt      album naklejek
    ├── components/            Sketch, Paper, Confetti, Icons, StickerArt, StreakStrip, …
    └── theme/Theme.kt         paleta Ink i style tekstu
```

Rysunki naklejek to dane, nie kod: `ui/components/StickerArt.kt` trzyma mapę identyfikator → lista warstw, gdzie warstwa to ścieżka SVG na siatce 24 × 24 i kolor z palety `Ink`. Rysuje je jedna funkcja, ta sama, która rysuje ikony, więc dodanie naklejki to dopisanie wpisu do mapy.

## Budowanie

Potrzebny jest JDK w `PATH` — Gradle uruchamia na nim swojego demona, zanim jeszcze przeczyta pliki buildu. JDK kompilujący kod pobiera się sam (toolchain 17 + `foojay-resolver`), więc wystarczy dowolna w miarę świeża Java:

```fish
sudo pacman -S jdk-openjdk        # albo: set -Ux JAVA_HOME ~/android-studio/jbr
```

Następnie:

```fish
./gradlew assembleDebug      # zbuduj APK
./gradlew installDebug       # zainstaluj na podłączonym urządzeniu
./gradlew clean              # wyczyść katalog build
```

Ścieżka do Android SDK trafia do `local.properties`, który nie jest wersjonowany. Android Studio generuje go przy pierwszym otwarciu projektu.

## Wydania

`bundleRelease` tworzy App Bundle w formacie oczekiwanym przez Google Play, z włączonym R8:

```fish
./gradlew bundleRelease
```

Podpisem sterują zmienne środowiskowe (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`), a w ich braku właściwości Gradle (`SB_STORE_FILE` i pokrewne) z `~/.gradle/gradle.properties`. Gdy nie ma żadnych, build release nadal działa i wychodzi niepodpisany.

`.github/workflows/release.yml` uruchamia to samo na tagu `v*`: testy jednostkowe, walidacja keystore'a, podpisany AAB i APK z sekretów repozytorium, weryfikacja przez `apksigner`, a na końcu podpięcie pod GitHub Release trzech plików z wersją w nazwie — `squared-board-v1.2.3.aab` do Google Play, `squared-board-v1.2.3.apk` do instalacji wprost z telefonu i `mapping-v1.2.3.txt`. Ten plik mapowania trzeba zachować — bez niego stack trace z R8 jest nieczytelny.

## Testy

80 testów jednostkowych, wszystkie działające na JVM:

```fish
./gradlew testDebugUnitTest
```

| Plik | Zakres |
| --- | --- |
| `QuestionGeneratorTest` | kształt równań w każdym trybie, dobór tabliczek, dystraktory, ważenie słabych działań |
| `ProgressTest` | punktacja, symetria klucza, próg opanowania |
| `LocalizationTest` | domyślne zasoby angielskie i angielska liczba mnoga |
| `ScreenRenderTest` | renderowanie każdego ekranu przez Robolectric (kwalifikator `pl`) |
| `AppFlowTest` | przejścia między ekranami, zmiana ustawień, start wyścigu i wyzwania dnia, album |
| `StreakTest` | przedłużenie i przerwanie serii, cofnięty zegar, pasek siedmiu dni |
| `StickersTest` | reguły zdobycia, kompletność katalogu, monotoniczność (naklejki nie da się stracić) |

Robolectric renderuje ekrany Compose na JVM, więc emulator nie jest potrzebny — umożliwia to ustawienie `testOptions { unitTests { isIncludeAndroidResources = true } }` w `app/build.gradle.kts`.

Raport HTML po uruchomieniu: `app/build/reports/tests/testDebugUnitTest/index.html`.

`ScreenshotGenerator` poza zrzutami do sklepu generuje też `9-wszystkie-naklejki.png` — cały komplet rysunków naraz, czyli najszybszy sposób, żeby obejrzeć ścieżki SVG po zmianie.

## Dostępność

- Aplikacja respektuje systemowe wyłączenie animacji (`ValueAnimator.areAnimatorsEnabled()`) — wtedy nie ma konfetti ani potrząsania kartą.
- Równania mają opis dla czytnika ekranu w formie mówionej (`Question.spoken()`): „3 razy 4 równa się ile". Słowa pochodzą z zasobów, więc czytnik mówi w języku telefonu.
- Kafelki planszy i przyciski tabliczek mają `contentDescription`.
- `ScaleToFit` zmniejsza równanie na wąskich ekranach zamiast je przycinać.
- Kolor tekstu na kafelkach planszy dobiera się do jasności tła (`Ink.onTier`).
