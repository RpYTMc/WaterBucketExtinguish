# Changelog

All notable changes to this project are documented here.

This project follows semantic versioning.

## [1.4.4] - 2026-11-03

### Changed
- Cloth Config is now optional. Without it the commands still work, and the settings key tells you how to get the settings screen.

### Fixed
- The HUD showed "Bucket Ready" when your hotbar only had an empty bucket, which can't put out fire. It now shows "Bucket Empty" for that case.

## [1.4.3] - 2026-09-14

### Added
- Minecraft 26.3 support (separate download, needs Java 25).

### Fixed
- Fire mode left you holding the bucket when the water could not be placed (blocked spot, water already there). The previous item is now restored right away.
- With both an empty and a water bucket in the hotbar the wrong one could be selected. Fire and cobweb now prefer the water bucket, refill prefers the empty one.
- A pending fire pickup no longer carries over after disconnecting or changing worlds.
- `/waterbucket config hudpos` with an invalid value now shows a message instead of an error.
- HUD hides with F1 and while the F3 debug screen is open.
- Failure hints now also cover Fire Resistance.

### Changed
- Statistics are written at most every 10 seconds and on exit instead of after every action.
- Config and statistics files tolerate empty or corrupt content and fall back to defaults.
- The mod is declared client-only, and Cloth Config is declared as a required dependency (the config screen always needed it).
- Removed the `/waterbucket reload` command (it never actually reloaded anything).

### Internal
- Removed leftover template mixins and the "Hello Fabric world" log line.
- Hotbar switching is documented as vanilla-equivalent (no manual slot packets), so anti-cheats see the same packets a normal player produces.

## [1.4.2] - 2026-02-21

### Improvements
- Major command system refactor
- Modular config command structure
- Cleaner help layout
- Added bucket readiness command
- Added HUD position setting
- Improved fire double-click timing (faster)
- Improved feedback system
- Reduced command duplication internally

### Technical
- Cleaner internal command architecture
- Reusable boolean/int setting builders
- Improved maintainability

## [1.4.1] - 2026-02-21

### Fixed
- Fire mode stats incorrectly counting pickup in certain edge cases
- Improved activation tracking consistency

### Internal
- Refined stat trigger logic for clearer behavior separation

## [1.4.0] - 2026-02-20

### Added
- Persistent statistics tracking system
- `/waterbucket stats` command
- `/waterbucket stats reset` command
- Accurate tracking for:
    - Fire extinguishes
    - Cobweb placements
    - Water pickups
    - Total successful actions
- JSON-based stat storage (`waterbucketstats.json`)
- Global stats tracking across all worlds and servers

### Improved
- Stats now increment only on successful actions
- Eliminated duplicate stat counting edge cases
- Improved action detection using item state validation
- Replaced `printStackTrace()` with proper logging

### Internal
- Refactored action tracking logic for consistency
- Stabilized stats architecture for future HUD integration

---

## [1.3.4] - 2026-02-20

### Added
- Full client-side command system (`/waterbucket`)
- Individual setting commands (pitch, sound, actionbar, nether)
- Reload & reset commands
- Improved boolean feedback formatting
- MIT License
- GitHub repository setup
- Project versioning + changelog system

---

## [1.3.3]

### Added
- Initial command framework
- Boolean toggle command support
- Config reload functionality

---

## [1.3.2]

### Added
- Mod Menu integration
- Cloth Config GUI
- Improved tooltip structure
- Organized config categories (Behavior, Feedback, Environment)

---

## [1.3.1]

### Added
- Language file support (`en_us.json`)
- Clean keybinding naming
- Custom keybinding category

---

## [1.3.0]

### Added
- JSON-based configuration system
- Pitch threshold setting
- Sound toggle
- Action Bar toggle
- Nether restriction option

---

## [1.2.1]

### Improved
- More reliable water pickup logic
- Better slot switching behavior

---

## [1.2.0]

### Added
- Water pickup anywhere logic
- Smarter hotbar bucket detection
- Cobweb-specific behavior rules

---

## [1.1.2]

### Improved
- Timing adjustments for double-click logic
- Reduced accidental misplacement

---

## [1.1.1]

### Fixed
- Keybinding issues
- Initial logic inconsistencies

---

## [1.1.0]

### Added
- Custom keybinding system
- Double-click extinguish logic

---

## [1.0.0]

### Initial Release
- Basic fire extinguish functionality
- Simple slot switching