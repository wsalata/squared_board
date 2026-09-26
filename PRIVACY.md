# Polityka prywatności — Tabliczka w kratkę

*[English version](PRIVACY.en.md)*

**Ostatnia aktualizacja: 26 września 2026**

## Krótko

Aplikacja **Tabliczka w kratkę** nie zbiera żadnych danych. Nie wysyła niczego do internetu, nie ma kont użytkowników, nie zawiera reklam ani narzędzi analitycznych. Wszystko, co zapamiętuje, zostaje na urządzeniu.

## Jakie dane są przetwarzane

Aplikacja zapisuje na urządzeniu wyłącznie postępy w nauce i ustawienia:

- wynik 0–5 dla każdego z działań tabliczki mnożenia,
- liczbę zdobytych gwiazdek i najlepszy wynik w trybie „Wyścig z czasem",
- wybrane ustawienia: rodzaj działań, wybrane tabliczki, sposób odpowiadania, dźwięk.

Te informacje nie pozwalają zidentyfikować nikogo. Nie są zbierane imiona, adresy e-mail, numery telefonu, identyfikatory urządzenia, lokalizacja ani żadne inne dane osobowe.

## Gdzie są przechowywane

Wyłącznie w pamięci urządzenia, w prywatnym magazynie aplikacji (Android DataStore). Nie są wysyłane na żaden serwer — ani nasz, ani cudzy. Nikt poza użytkownikiem telefonu nie ma do nich dostępu.

Aplikacja **nie ma uprawnienia do korzystania z internetu**. Można to sprawdzić samodzielnie: w pliku `AndroidManifest.xml` widnieje jedno uprawnienie, `android.permission.VIBRATE`, potrzebne do krótkiej wibracji przy odpowiedzi. Bez uprawnienia `INTERNET` system Android uniemożliwia aplikacji jakiekolwiek połączenie sieciowe.

## Usuwanie danych

Postępy można wyzerować w samej aplikacji: ekran **Moja tabliczka**, przycisk **Wyzeruj postępy** (wymaga potwierdzenia drugim dotknięciem). Odinstalowanie aplikacji również usuwa wszystkie zapisane dane.

## Dzieci

Aplikacja jest przeznaczona dla dzieci w wieku wczesnoszkolnym i została zaprojektowana tak, by nie zbierać o nich żadnych informacji. Nie ma w niej reklam, zakupów w aplikacji, linków zewnętrznych, mediów społecznościowych ani komunikacji z innymi użytkownikami. Nie korzysta z żadnych zewnętrznych bibliotek reklamowych ani analitycznych.

## Uprawnienia

| Uprawnienie | Po co |
| --- | --- |
| `VIBRATE` | krótka wibracja przy poprawnej i błędnej odpowiedzi |

To jedyne uprawnienie, o jakie prosi aplikacja.

## Zmiany

Ewentualne zmiany tej polityki będą publikowane pod tym samym adresem, ze zaktualizowaną datą na górze strony.

## Kontakt

Pytania o prywatność: **walery.salata@gmail.com**

Kod źródłowy aplikacji jest jawny: https://github.com/wsalata/squared_board
