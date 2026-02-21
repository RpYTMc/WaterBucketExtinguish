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
  <img src="https://img.shields.io/badge/Minecraft-1.21.11-brightgreen" />
  <img src="https://img.shields.io/badge/Loader-Fabric-orange" />
  <img src="https://img.shields.io/badge/License-MIT-blue" />
  <img src="https://img.shields.io/github/v/release/RpYTMc/WaterBucketExtinguish?color=blueviolet" />
  <img src="https://img.shields.io/github/downloads/RpYTMc/WaterBucketExtinguish/total" />
</p>

# Water Bucket Extinguish

A semi PvP-oriented Fabric utility mod that makes water bucket usage faster, smarter, and more intuitive.

---

## ⚠ Disclaimer

This project was developed with the assistance of AI (ChatGPT 5.2).  
Architecture decisions, integration, testing, debugging, and feature design were handled manually by the project author.

AI was used as a development assistant, not as an autonomous code generator.

---

##  Features

- Smart fire extinguish double-click logic
- Faster fire re-pickup timing
- Cobweb escape support
- Intelligent water pickup detection
- Bucket readiness detection (hotbar / inventory / none)
- Clean in-game command system
- Config GUI (Cloth Config support)
- HUD position customization
- Statistics tracking
- Nether restriction toggle
- Sound + Action Bar feedback
- Fully modular command architecture

---

##  HUD System

The HUD can display bucket readiness:

- ✔ Bucket Ready (in hotbar)
- ⚠ Bucket in Inventory
- ✖ No Bucket Found

Position can be configured:
- Top Left
- Top Right
- Bottom Left
- Bottom Right

---

##  Commands

### Base

`/waterbucket`  
Shows mod info and version.

`/waterbucket help`  
Shows full command list.

---

### Utility

`/waterbucket bucket`  
Check bucket readiness.

`/waterbucket stats`  
View statistics.

`/waterbucket stats reset`  
Reset statistics.

`/waterbucket reload`  
Reload configuration from file.

---

### Configuration

`/waterbucket config pitch <value>`  
`/waterbucket config pitch reset`

`/waterbucket config sound <true|false>`  
`/waterbucket config actionbar <true|false>`  
`/waterbucket config nether <true|false>`  
`/waterbucket config hud <true|false>`

`/waterbucket config hudpos <top_left|top_right|bottom_left|bottom_right>`  
`/waterbucket config hudpos reset`

---

##  Dependencies

### Required
- Fabric Loader
- Fabric API

### Optional
- Cloth Config
- Mod Menu

---

##  Technical Notes

- Client-side mod
- Java 21+

---

##  License

MIT License