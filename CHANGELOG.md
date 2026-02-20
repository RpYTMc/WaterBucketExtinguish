# Changelog

All notable changes to this project are documented here.

This project follows semantic versioning.

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