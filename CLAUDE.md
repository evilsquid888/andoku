# CLAUDE.md

## Project Overview

Andoku is an open-source Sudoku puzzle game for Android, originally by Markus Wiederkehr. It is licensed under GPLv3. The app supports multiple Sudoku variants, themes, puzzle importing, and localization (English, German, French, Italian, Finnish).

**Package:** `com.googlecode.andokusquid`
**Target SDK:** Android 8 (API level 8 / Froyo 2.2)
**Min SDK:** Android 3 (API level 3 / Cupcake 1.5)
**Version:** 1.3.4

## Repository Structure

The repo contains multiple Eclipse-based Android projects (no Gradle/Maven):

| Directory | Purpose |
|---|---|
| `Andoku/` | Main Android application (81 Java source files) |
| `Andoku-Client/` | Client library, packaged as `andoku-client-1.0.0.jar` |
| `Andoku-Client-Demo/` | Demo app showing client library usage |
| `Andoku-Web-Import/` | Companion app for importing puzzles from the web |
| `Andoku-Tests/` | Android instrumentation tests |
| `Themes/` | Theme artwork (GIMP `.xcf` files and exported PNGs) |

## Build & Run

This is a legacy Eclipse/Ant Android project (no Gradle). To build:

1. Build `Andoku-Client` first (erase `gen/` directory, then build the JAR).
2. In `Andoku/src/.../BackupUtil.java`, set the secret key string.
3. Build the `Andoku` project.
4. Deploy as an Android app.

See `tobuild.txt` for the original build notes.

## Testing

Tests live in `Andoku-Tests/` and are Android instrumentation tests targeting API 8. Test classes:

- `AndokuPuzzleTest` -- model-level puzzle logic tests
- `AndokuDatabaseSaveGameTest` -- save/load game persistence tests
- `AndokuDatabasePuzzleTest` -- puzzle database operations
- `AndokuDatabaseFolderTest` -- folder management in the database
- `UpdateAndokuDatabaseTest` -- database migration/upgrade tests
- `MockPuzzleSource` -- test utility mock

Run tests via Eclipse Android test runner or `adb` instrumentation commands.

## Key Architecture

### Main App (`Andoku/src/com/googlecode/andoku/`)

- **Activities:** `MainActivity` (launcher), `AndokuActivity` (game play), `NewGameActivity`, `ResumeGameActivity`, `FolderListActivity`, `SettingsActivity`, `HelpActivity`, `AboutActivity`
- **`AndokuPuzzleView`** -- custom `View` rendering the Sudoku grid
- **`AndokuContentProvider`** -- exposes puzzles for import by other apps
- **`Theme` / `ColorTheme`** -- visual theming system

### Sub-packages

- **`model/`** -- puzzle data model and game state
- **`solver/`** -- Sudoku solving algorithms
- **`dlx/`** -- Dancing Links (DLX) exact cover solver implementation
- **`db/`** -- SQLite database layer for puzzles and saved games
- **`source/`** -- puzzle source abstraction (`AssetsPuzzleSource`, `DbPuzzleSource`)
- **`transfer/`** -- puzzle encoding/decoding and standard area definitions
- **`im/`** -- input method handling
- **`history/`** -- undo/redo command history
- **`commands/`** -- command pattern implementations for game actions

### Assets

- `Andoku/assets/puzzles/` -- bundled puzzle data files
- `Andoku/assets/*.html` -- help and about pages (localized)

## Coding Conventions

- Java source follows standard Android/Java conventions circa 2009-2010
- Tabs for indentation in XML; mixed tabs/spaces in Java
- All activities are portrait-locked
- Copyright headers reference GPLv3 in source files
- Package root: `com.googlecode.andoku`

## License

GNU General Public License v3.0 -- see `Andoku/COPYING`.
