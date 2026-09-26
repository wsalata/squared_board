# Privacy Policy — Times Tables Grid

*[Wersja polska](PRIVACY.md)*

**Last updated: 26 September 2026**

## In short

**Times Tables Grid** collects no data. It sends nothing to the internet, has no user accounts, and contains no advertising or analytics. Everything it remembers stays on the device.

## What data is processed

The app stores only learning progress and settings, on the device:

- a 0–5 score for each multiplication fact,
- the number of stars earned and the best score in "Race the clock",
- the chosen settings: kind of facts, selected tables, answering method, sound.

None of this identifies anyone. No names, email addresses, phone numbers, device identifiers, location or any other personal data are collected.

## Where it is stored

Only in the device's memory, in the app's private storage (Android DataStore). It is never sent to any server — ours or anyone else's. Nobody but the person using the phone can reach it.

The app **has no internet permission**. You can verify this yourself: `AndroidManifest.xml` declares a single permission, `android.permission.VIBRATE`, used for the short buzz on an answer. Without the `INTERNET` permission, Android prevents the app from making any network connection at all.

## Deleting the data

Progress can be cleared inside the app: the **My board** screen, the **Reset progress** button (it asks for a confirming second tap). Uninstalling the app also removes everything it stored.

## Children

The app is intended for children in their first school years and is built so that it collects nothing about them. It contains no advertising, no in-app purchases, no external links, no social media and no communication with other users. It uses no third-party advertising or analytics libraries.

## Permissions

| Permission | What for |
| --- | --- |
| `VIBRATE` | a short buzz on a correct or wrong answer |

This is the only permission the app requests.

## Changes

Any changes to this policy will be published at the same address, with an updated date at the top of the page.

## Contact

Privacy questions: **walery.salata@gmail.com**

The app's source code is public: https://github.com/wsalata/squared_board
