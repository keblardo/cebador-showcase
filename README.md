<p align="center"><img src="screenshots/icon.png" width="96" alt="Cebador mascot"></p>

<h1 align="center">Cebador — mate counter & food diary</h1>

<p align="center">
  Android (Java, WebView + native widget) · Windows desktop (Python, pywebview) · HTML/JS
</p>

> **Portfolio excerpt — not the full source.** A few files from a small personal project, shared so reviewers can
> see how I write code. All rights reserved (see [LICENSE](LICENSE)).

## What it is

A personal app for mate drinkers: it counts the mates you serve in a session (bitter or sweet), keeps track of water
and of the sugar and snacks that go with it, and has a food and calorie diary backed by a local database of
about 190 foods with per-unit macros. I built it for myself; it is not on Google Play.

- **Android:** the UI is HTML/JS in a WebView. A native **4×1 home-screen widget** lets you add a mate, switch
  bitter/sweet and add a snack without opening the app. The WebView and the widget share one session JSON through a
  JavaScript bridge; an `updatedAt` timestamp decides which side has the newest copy.
- **Windows:** the same HTML/JS runs in a small always-on-top window with pywebview, and opens the diary in a second
  window. Packaged with PyInstaller.

## Status

| | |
|---|---|
| Built | September 3–9, 2026 |
| Distribution | Personal use, not published |

## What's in this excerpt

| File | What it shows |
|---|---|
| `app/.../CebadorWidget.java` | `AppWidgetProvider` with `RemoteViews`, broadcast actions per button and live refresh |
| `app/.../SessionStore.java` | Session state shared by the widget and the WebView, the same counting rules as the JS app, and calorie totals |
| `app/.../MainActivity.java` | WebView host and the `@JavascriptInterface` bridge to the widget |
| `desktop/app.py` | pywebview desktop shell: always-on-top window, diary window opened from JS, logging for the frozen build |

Java files are under `app/src/main/java/com/cebador/app/`. Comments and UI strings are in Spanish.

**Not included on purpose:** the HTML/JS app, the food and recipe data, images, and build files.

---

Built by **Cristian Luis Celie**: [GitHub profile and CV](https://github.com/keblardo) · cristiancelie07@gmail.com
