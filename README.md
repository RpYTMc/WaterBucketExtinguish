<!-- github-only -->
<p align="center">
  <img src="https://cdn.modrinth.com/data/oyLBBIDr/df503606b7564309ee616b76a9c292526331dbf4.png" width="96" height="96" alt="Water Bucket Extinguish icon" />
</p>

<h1 align="center">Water Bucket Extinguish</h1>

<p align="center">
  One key puts out fire, frees you from cobwebs and refills your bucket,<br />
  then hands your weapon straight back.
</p>

<p align="center">
  <a href="https://modrinth.com/mod/water-bucket-extinguish"><img src="https://img.shields.io/badge/Modrinth-Download-1bd96a?style=for-the-badge&logo=modrinth&logoColor=white" alt="Download on Modrinth" /></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/water-bucket-extinguish"><img src="https://img.shields.io/badge/CurseForge-Download-f16436?style=for-the-badge&logo=curseforge&logoColor=white" alt="Download on CurseForge" /></a>
  <a href="https://github.com/RpYTMc/WaterBucketExtinguish/releases"><img src="https://img.shields.io/badge/GitHub-Releases-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub releases" /></a>
</p>

<p align="center">
  <img src="https://img.shields.io/modrinth/game-versions/water-bucket-extinguish?label=Minecraft&color=brightgreen" alt="Minecraft versions" />
  <img src="https://img.shields.io/badge/loader-Fabric-dbd0b4" alt="Fabric" />
  <img src="https://img.shields.io/modrinth/dt/water-bucket-extinguish?label=Modrinth&logo=modrinth&color=1bd96a" alt="Modrinth downloads" />
  <img src="https://img.shields.io/curseforge/dt/1467538?label=CurseForge&logo=curseforge&color=f16436" alt="CurseForge downloads" />
  <img src="https://img.shields.io/badge/license-MIT-blue" alt="MIT license" />
</p>
<!-- /github-only -->

A client-side Fabric mod for PvP and survival. Press one key and the mod switches to your water bucket, uses it, picks the water back up and gives you back whatever you were holding. It works on any server, vanilla ones included, and needs nothing installed on the server.

<!-- github-only -->
<p align="center">
  <a href="https://youtu.be/0nmwsDrVasU"><img src="https://img.youtube.com/vi/0nmwsDrVasU/maxresdefault.jpg" width="640" alt="Watch the demo video on YouTube" /></a><br />
  <sub>Click the picture to watch the demo on YouTube.</sub>
</p>
<!-- /github-only -->
<!-- listing-only
<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/0nmwsDrVasU" frameborder="0" allowfullscreen></iframe>
-->

## Which version do I need?

Download the file whose name ends with your Minecraft version:

| Minecraft | File name ends with | Java |
|---|---|---|
| 26.3 | `+mc26.3.jar` | 25 |
| 26.2 | `+mc26.2.jar` | 25 |
| 26.1, 26.1.1, 26.1.2 | `+mc26.1.jar` | 25 |
| 1.21.11 | `+mc1.21.11.jar` | 21 |
| 1.20.1 | `+mc1.20.1.jar` | 17 |

Every version needs [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api). [Cloth Config](https://modrinth.com/mod/cloth-config) adds the settings screen and [Mod Menu](https://modrinth.com/mod/modmenu) adds a settings button to the mod list; both are optional. The official Minecraft launcher already comes with the right Java.

## How to use it

Press <kbd>]</kbd>. What it does depends on the situation:

| Situation | What the key does |
|---|---|
| On fire, standing on the ground, looking down | Places water at your feet, picks it up again a moment later and switches back to your item |
| Stuck in a cobweb | Places water to wash the cobweb away. Press again to pick the water back up |
| Not on fire, with an empty bucket in the hotbar | Fills the empty bucket from the water you are looking at |

Press <kbd>P</kbd> to open the settings (needs Cloth Config). You can change both keys in Options, Controls, Key Binds.

The bucket has to be in your hotbar. With a full and an empty bucket there, the mod uses the right one for the job. If the key can't do anything, a short message tells you why, for example "Look down to place water" or "No bucket in hotbar". By default the key does nothing in the Nether, where water can't be placed anyway.

## Safe to use on servers

The mod only switches your hotbar slot and right-clicks, through the same game code a normal key press uses. It never fakes your position or where you are looking, and it never sends network packets of its own. Servers and anti-cheats see the same thing they would see from a player doing it by hand. Some servers don't allow utility mods at all, so check the rules of the server you play on.

## Bucket HUD

A small label in a corner of the screen shows whether a bucket is ready:

| HUD | Meaning |
|---|---|
| <img src="https://cdn.modrinth.com/data/cached_images/a95c89823bb1640213a3a8c1bde4568c1f8a0acf.png" alt="Bucket Ready" /> | A bucket is in your hotbar |
| ⚠ Bucket Empty | Only an empty bucket is in your hotbar: it can refill, but it can't put out fire |
| <img src="https://cdn.modrinth.com/data/cached_images/8719112b74f42e1cbade2ad3afdf9e956f8cdc98.png" alt="Bucket in Inventory" /> | A bucket is in your inventory, move it to the hotbar |
| <img src="https://cdn.modrinth.com/data/cached_images/562d7218e8e9608ceca7173ac281de7ea0788a08.png" alt="No Bucket" /> | You have no bucket |

You can move it to any corner or turn it off. It hides with F1 and while the F3 screen is open.

## Settings and commands

<p align="center">
  <img src="https://cdn.modrinth.com/data/oyLBBIDr/images/6e19bac39937a9cf7551deb733f91455a0cb3aa3.png" width="640" alt="The settings screen" />
</p>

Every setting can also be changed with a command:

<details>
<summary>All <code>/waterbucket</code> commands</summary>

| Command | What it does |
|---|---|
| `/waterbucket` | Shows the mod version |
| `/waterbucket help` | Lists all commands |
| `/waterbucket bucket` | Tells you whether a bucket is ready |
| `/waterbucket stats` | Shows how often the mod has helped you |
| `/waterbucket stats reset` | Resets the statistics |
| `/waterbucket config pitch <0-90>` | How far down you must look for the fire case (default 67) |
| `/waterbucket config sound <true/false>` | Sound when the key does something |
| `/waterbucket config actionbar <true/false>` | Short messages above the hotbar |
| `/waterbucket config nether <true/false>` | Turn the key off in the Nether |
| `/waterbucket config hud <true/false>` | Show the bucket HUD |
| `/waterbucket config hudpos <corner>` | Where the HUD sits: `top_left`, `top_right`, `bottom_left` or `bottom_right` |

Add `reset` to any setting to restore its default, for example `/waterbucket config pitch reset`.

</details>

Settings are saved in `config/waterbucketconfig.json` and statistics in `config/waterbucketstats.json`.

## Links

- [Changelog](https://github.com/RpYTMc/WaterBucketExtinguish/blob/HEAD/CHANGELOG.md)
- [Report a bug or suggest a feature](https://github.com/RpYTMc/WaterBucketExtinguish/issues)
- [Source code](https://github.com/RpYTMc/WaterBucketExtinguish), with build and release notes in [CONTRIBUTING.md](https://github.com/RpYTMc/WaterBucketExtinguish/blob/HEAD/CONTRIBUTING.md)

## License

[MIT](https://github.com/RpYTMc/WaterBucketExtinguish/blob/HEAD/LICENSE). Made by RpYT.
