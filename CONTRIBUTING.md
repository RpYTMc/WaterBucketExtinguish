# Building, porting and releasing

This file is for people who build, port or release the mod. Players only need the [README](README.md).

## Branches

There is one branch per Minecraft version, named `mc/` plus the Minecraft version:

| Branch | Minecraft | Java | Notes |
|---|---|---|---|
| `mc/26.1` | 26.1, 26.1.1, 26.1.2 | 25 | Mojang names, no remapping (26.1 is unobfuscated). Has the in-game test |
| `mc/1.21.11` | 1.21.11 | 21 | Yarn names |
| `mc/1.20.1` | 1.20.1 | 17 | Yarn names |

The default branch on GitHub is the newest Minecraft version. A fix goes to every branch that still gets updates. `README.md`, `CONTRIBUTING.md` and `LICENSE` are the same on every branch, so copy changes to all of them.

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`. Gradle has to run on a JDK at least as new as the branch's Java version. JDK 25 builds every branch.

## In-game test

On `mc/26.1` and newer branches:

```bash
./gradlew runClientGameTest
```

It opens a real game window, creates a world and plays the fire, refill and cobweb cases, then checks the commands, the HUD and the settings screen. It takes about a minute. Screenshots are saved to `build/run/clientGameTest/screenshots/`.

## Porting to a new Minecraft version

1. Create `mc/<new version>` from the newest branch.
2. Update `gradle.properties`: `minecraft_version`, the Fabric Loader, Loom, Fabric API, Cloth Config and Mod Menu versions, `mod_version` and `publish_minecraft_versions`. If the Java version changes, update it in `build.gradle`, `fabric.mod.json` and both workflow files too. Fabric's [porting guide](https://docs.fabricmc.net/develop/porting/) lists what changed in each version.
3. Build, fix what broke and run the in-game test.
4. Add the version to the table in `README.md` and to the branch table above, on every branch.
5. Make the new branch the default branch on GitHub.

## Releasing

Releases are published by GitHub Actions when a version tag is pushed. The workflow is `.github/workflows/release.yml`, and it runs `./gradlew publishMods`.

One-time setup, in the repository settings under Secrets and variables, Actions:

- `MODRINTH_TOKEN`: a Modrinth personal access token (modrinth.com/settings/pats) with the scopes Create versions, Read versions, Write versions, Read projects and Write projects. The project scopes let a release update the Modrinth description.
- `CURSEFORGE_TOKEN`: a CurseForge API token (legacy.curseforge.com/account/api-tokens).

For each release, on each branch you release from:

1. Set `mod_version` in `gradle.properties` and add a `## [x.y.z]` section to `CHANGELOG.md`. That section becomes the release notes.
2. Commit and push the branch.
3. Push a tag equal to `mod_version` with `+` replaced by `-`, for example `git tag -a 1.4.3-mc1.21.11 -m "Water Bucket Extinguish 1.4.3"` then `git push origin 1.4.3-mc1.21.11`.

The workflow uploads the jar to Modrinth, CurseForge and a GitHub release. The Minecraft versions each upload is listed for come from `publish_minecraft_versions`. The workflow stops when the tag doesn't match `mod_version` or a secret is missing. Starting it by hand from the Actions tab, or running `./gradlew publishMods` without the tokens, is a dry run that uploads nothing.

## Store descriptions

Each release also replaces the Modrinth description with `README.md`, adjusted for the store page:

- Everything between `<!-- github-only -->` and `<!-- /github-only -->` is left out. That is the header and the video thumbnail, because Modrinth shows its own title, icon and buttons.
- Text inside a `<!-- listing-only ... -->` comment is shown only on the store page. That is the embedded video player, which GitHub can't show.

CurseForge has no API for descriptions. Run `./gradlew listingDescription` and paste `build/listing-description.md` into the CurseForge description by hand.
