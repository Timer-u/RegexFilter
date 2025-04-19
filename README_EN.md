[繁體中文](./README_TW.md) | [简体中文](./README.md) | **English**

# RegexFilter

A Fabric-based Minecraft mod that supports filtering chat messages using regular
expressions, featuring a visual configuration interface powered by Cloth Config
API.

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## Dependencies

| Dependency       | Type     | Version    | Links                                                                                                                            |
| ---------------- | -------- | ---------- | -------------------------------------------------------------------------------------------------------------------------------- |
| Minecraft        | Required | 1.21       | [Official Website](https://www.minecraft.net/)                                                                                   |
| Fabric Loader    | Required | >=0.15.0   | [Github](https://github.com/FabricMC/fabric-loader)                                                                              |
| Fabric API       | Required | Any        | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api) &#124; [Modrinth](https://modrinth.com/mod/fabric-api)     |
| Cloth Config API | Required | >=15.0.127 | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cloth-config) &#124; [Modrinth](https://modrinth.com/mod/cloth-config) |
| Mod Menu         | Required | >=11.0.0   | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/modmenu) &#124; [Modrinth](https://modrinth.com/mod/modmenu)           |

## Usage Guide

1. Install all dependencies.
2. Launch Minecraft and navigate to the main menu.
3. Click `Mods` → Locate this mod → Click the mod icon.
4. Adjust the following settings:
   - `Enable Mod`: Toggle the mod on/off.
   - `Regex List`: Enter the regular expressions for filtering.

## Regular Expression Examples

| Use Case                               | Regular Expression       | Description                                                                                                                         |
| -------------------------------------- | ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| Filter messages from a specific player | `^\[?\w+\]? >.*`         | Matches formats like `[Player] > Message` or `Player: Message`. Replace `\w+` with the actual player name (e.g., `^Steve >.*`).     |
| Filter messages with sensitive words   | `(?i).*?(cheat\|hack).*` | Case-insensitive matching for sensitive words anywhere in the message, supporting fuzzy matches (e.g., "using hacks" or "cheater"). |
| Filter system announcements            | `^\\[System\\].*`        | Matches messages starting with `[System]` (double backslashes are required in the config file for escaping).                        |
| Filter colored rule-breaking messages  | `§[0-9a-fk-or].*rule.*`  | Matches messages containing color/format codes (e.g., `§c`) and the word "rule" (case-insensitive, e.g., `§cRule Violation`).       |

## License

This project is licensed under the
**[GNU General Public License v3.0](LICENSE)**. You must comply with the
following terms:

- **Open Source Requirement**  
  Any modifications or derivative works based on this project must **remain open
  source** and be released under the same license (GPL v3.0).
- **License Preservation**  
  Distributed copies must include the complete [LICENSE](LICENSE) file and the
  original copyright notice.
- **Source Code Provision**  
  If distributing binaries, the corresponding complete and runnable source code
  must be provided (via repository link or attached files).
- **No Warranty**  
  The author provides no warranties for the code's suitability or stability.
  Users assume all risks.
