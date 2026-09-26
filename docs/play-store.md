# Wysyłka do Google Play — lista kroków

Stan przygotowań i kolejność działań. Zaznaczaj, co zrobione.

## Gotowe w repozytorium

- [x] Podpisany AAB z CI (`play/` zawiera materiały do listingu)
- [x] Ikona 512 × 512 — `play/icon-512.png`
- [x] Grafika promocyjna 1024 × 500 — `play/feature-graphic-pl.png`, `play/feature-graphic-en.png`
- [x] Teksty listingu — `play/listing-pl.md`, `play/listing-en.md`
- [x] Polityka prywatności — `PRIVACY.md`, `PRIVACY.en.md`
- [x] `targetSdk 37`, brak uprawnienia `INTERNET`, zero reklam i analityki

## 1. Zanim wejdziesz do konsoli

- [ ] Wpisz swój adres e-mail w miejsce `<TWÓJ-ADRES-E-MAIL>` w obu politykach prywatności
- [ ] Wystaw politykę pod publicznym adresem (patrz niżej) i zapisz URL
- [ ] Zrób 4–6 zrzutów ekranu na telefonie lub emulatorze (ekran główny, gra, klawiatura, wynik, plansza)
- [ ] Upewnij się, że keystore ma kopię poza dyskiem

### Hosting polityki prywatności przez GitHub Pages

1. *Settings → Pages → Source: Deploy from a branch → `main` / `/ (root)`*
2. Po kilku minutach polityka będzie pod:
   `https://wsalata.github.io/squared_board/PRIVACY` (lub `.../PRIVACY.html`)
3. Ten adres wklejasz w konsoli. Musi być publiczny i działać bez logowania.

## 2. Konto deweloperskie

- [ ] Rejestracja w Play Console, 25 USD jednorazowo
- [ ] **Konto osobiste:** zamknięte testy z 12 testerami przez 14 kolejnych dni,
      zanim Google odblokuje publikację produkcyjną. To najdłuższy element całej
      układanki — zacznij od niego, nie od grafik.
- [ ] **Konto firmowe:** numer D-U-N-S zamiast testów

## 3. Utwórz aplikację

- [ ] *Create app*: nazwa, język domyślny, typ „Gra" lub „Aplikacja", darmowa
- [ ] Domyślny język listingu: polski (`pl-PL`), drugi wpis: angielski (`en-US`)

## 4. Formularze (sekcja „Zasady aplikacji")

- [ ] **Polityka prywatności** — URL z kroku 1
- [ ] **Reklamy** — „Aplikacja nie zawiera reklam"
- [ ] **Data safety** — nie zbieramy i nie udostępniamy żadnych danych.
      Uzasadnienie: brak uprawnienia `INTERNET`, dane tylko w DataStore na urządzeniu
- [ ] **Kwestionariusz IARC** — ocena wiekowa
- [ ] **Target audience and content** — zadeklaruj grupę wiekową zgodnie z prawdą.
      Poniżej 13 lat uruchamia **Families Policy**: polityka prywatności staje się
      obowiązkowa (masz ją), a recenzja trwa dłużej. Aplikacja spełnia wymogi już teraz —
      bez reklam, bez zakupów, bez linków zewnętrznych, bez SDK analitycznych.
      Nie dodawaj Firebase Analytics „na szybko" przed wydaniem.
- [ ] **Government apps / Financial features / Health** — nie dotyczy

## 5. Listing

- [ ] Tytuł, krótki i pełny opis — z `play/listing-pl.md` i `play/listing-en.md`
- [ ] Ikona — `play/icon-512.png`
- [ ] Grafika promocyjna — `play/feature-graphic-pl.png`
- [ ] Zrzuty ekranu (min. 2, sensownie 4–6)
- [ ] Kategoria: Edukacja. Adres kontaktowy: twój e-mail

## 6. Wydanie

- [ ] Otaguj wydanie: `git tag v1.0.0 && git push origin v1.0.0`
- [ ] Pobierz `squared-board-v1.0.0.aab` z GitHub Release
- [ ] Wgraj w *Production* (albo najpierw *Closed testing*)
- [ ] Zachowaj `mapping-v1.0.0.txt` — bez niego stack trace z R8 jest nieczytelny
- [ ] Wyślij do recenzji

## Po pierwszym wydaniu

- `applicationId` = `eu.tudek.squared_board` jest **nieodwracalne**
- Każdy kolejny plik musi mieć wyższy `versionCode` (CI bierze go z numeru przebiegu)
- APK z GitHub Release jest podpisany kluczem uploadu, nie kluczem Google —
  nie zaktualizuje wersji zainstalowanej ze sklepu
