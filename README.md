<p style="text-align:center;">
  <a href="https://modrinth.com/mod/water-bucket-extinguish">
    <img src="https://img.shields.io/badge/Download-Modrinth-1bd96a?style=for-the-badge&logo=modrinth&logoColor=white" alt="Download on Modrinth" />
  </a>
  <a href="https://github.com/RpYTMc/WaterBucketExtinguish/releases">
    <img src="https://img.shields.io/badge/Source-GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="Source on GitHub" />
  </a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/water-bucket-extinguish">
    <img src="https://img.shields.io/badge/Download-CurseForge-f16436?style=for-the-badge&logo=curseforge&logoColor=white" alt="Download on CurseForge" />
  </a>
</p>

<p style="text-align:center;">
  <img src="https://img.shields.io/badge/Minecraft-1.21.11%20%7C%201.20.1-brightgreen" />
  <img src="https://img.shields.io/badge/Loader-Fabric-orange" />
  <img src="https://img.shields.io/badge/License-MIT-blue" />
  <img src="https://img.shields.io/github/v/release/RpYTMc/WaterBucketExtinguish?color=blueviolet" />
  <img src="https://img.shields.io/github/downloads/RpYTMc/WaterBucketExtinguish/total" />
</p>

# Water Bucket Extinguish

A client-side Fabric PvP utility mod. One key switches to your water bucket, places it, picks it back up, and hands you back whatever you were holding.

Works on vanilla servers. Nothing to install server-side.

---

## Usage

- **`]`** (configurable): the panic key. What it does depends on the situation:
  - **On fire**, standing on the ground and looking down: places water at your feet, picks it up a tick later, switches back to your previous item.
  - **Stuck in a cobweb**: places water so you can swim out. Press again to pick the water back up.
  - **Holding an empty bucket** (not on fire): refills it from the water you are looking at.
- **`P`** (configurable): opens the config screen.
- If the key can't do anything, a short hint tells you why ("Look down to place water", "Not on fire", "No bucket in hotbar", ...).

If you carry both a water bucket and an empty one, the mod picks the right one for the job.

---

## How it behaves on servers

The mod only calls the same client code vanilla uses for a right-click and a hotbar switch. It never fakes your position or rotation and never sends packets of its own, so servers and anti-cheats see the exact packet sequence a regular player produces.

---

## Features

- Smart fire extinguish with automatic re-pickup
- Cobweb escape support
- Bucket refill with one key
- Bucket readiness HUD (hotbar / inventory / none), four corner positions
- Config GUI (Cloth Config) and a full `/waterbucket` command set
- Usage statistics
- Nether restriction toggle
- Sound and action bar feedback

---

## HUD

The HUD shows bucket readiness:

- ✔ Bucket Ready (in hotbar)
- ⚠ Bucket in Inventory
- ✖ No Bucket Found

Positions: Top Left, Top Right, Bottom Left, Bottom Right. The HUD hides with F1 and while the F3 screen is open.

---

## Commands

### Base

`/waterbucket`  
Shows mod info and version.

`/waterbucket help`  
Shows the full command list.

### Utility

`/waterbucket bucket`  
Check bucket readiness.

`/waterbucket stats`  
View statistics.

`/waterbucket stats reset`  
Reset statistics.

### Configuration

`/waterbucket config pitch <0-90>`  
`/waterbucket config pitch reset`

`/waterbucket config sound <true|false>`  
`/waterbucket config actionbar <true|false>`  
`/waterbucket config nether <true|false>`  
`/waterbucket config hud <true|false>`

`/waterbucket config hudpos <top_left|top_right|bottom_left|bottom_right>`  
`/waterbucket config hudpos reset`

Settings are stored in `config/waterbucketconfig.json`, statistics in `config/waterbucketstats.json`.

---

## Dependencies

### Required
- Fabric Loader
- Fabric API
- Cloth Config

### Optional
- Mod Menu (adds a config button in the mod list)

---

## Technical Notes

- Client-side only
- `main` branch: Minecraft 1.21.11, Java 21
- `1.20.1-port` branch: Minecraft 1.20.1, Java 17
- Parts of the code were written with AI assistance. Design, testing and releases are by RpYT.

---

## License

MIT License
