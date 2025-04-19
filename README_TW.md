**繁體中文** | [简体中文](./README.md) | [English](./README_EN.md)

# RegexFilter

一個基於 Fabric 的 Minecraft 模組，支援透過正則表達式過濾聊天訊息，提供基於 Cloth
Config API 的可視化設定介面。

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## 依賴項目

| 依賴             | 類型 | 版本       | 連結                                                                                                                             |
| ---------------- | ---- | ---------- | -------------------------------------------------------------------------------------------------------------------------------- |
| Minecraft        | 必須 | 1.21       | [Official Website](https://www.minecraft.net/)                                                                                   |
| Fabric Loader    | 必須 | >=0.15.0   | [Github](https://github.com/FabricMC/fabric-loader)                                                                              |
| Fabric API       | 必須 | 任意       | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api) &#124; [Modrinth](https://modrinth.com/mod/fabric-api)     |
| Cloth Config API | 必須 | >=15.0.127 | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cloth-config) &#124; [Modrinth](https://modrinth.com/mod/cloth-config) |
| Mod Menu         | 必須 | >=11.0.0   | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/modmenu) &#124; [Modrinth](https://modrinth.com/mod/modmenu)           |

## 使用指南

1. 安裝所有依賴項目。
2. 啟動 Minecraft 並進入主選單。
3. 點擊 `Mods` → 找到本模組 → 點擊模組圖示。
4. 調整以下設定：
   - `啟用模組`：總開關。
   - `正則列表`：輸入要過濾的正則表達式。

## 正則表達式範例

| 使用場景                 | 正則表達式              | 說明                                                                                        |
| ------------------------ | ----------------------- | ------------------------------------------------------------------------------------------- |
| 過濾特定玩家的訊息       | `^\[?\w+\]? >.*`        | 適配 `[玩家名] > 訊息` 或 `玩家名: 訊息` 格式，需替換 `\w+` 為實際玩家名（如 `^Steve >.*`） |
| 過濾含敏感詞的訊息       | `(?i).*?(作弊\|外掛).*` | 不區分大小寫匹配任意位置的敏感詞，支援模糊匹配（如"使用外掛"或"作弊者"）                    |
| 過濾系統公告類訊息       | `^\\[系統\\].*`         | 匹配以 `[系統]` 開頭的訊息（設定檔需保留雙反斜槓轉義）                                      |
| 過濾帶顏色代碼的違規訊息 | `§[0-9a-fk-or].*違規.*` | 匹配包含顏色/格式代碼（如 `§c`）且含"違規"的訊息，支援全小寫（如 `§c違規警告`）             |

## 開源協議

本專案採用 **[GNU General Public License v3.0](LICENSE)**
協議，您必須遵守以下條款：

- **開源義務**  
  任何基於本專案的修改或衍生作品必須**保持開源**，並使用相同協議（GPL v3.0）發布。
- **協議保留**  
  在分發的副本中必須包含完整的 [LICENSE](LICENSE) 文件及原始版權聲明。
- **原始碼提供**  
  若分發二進位文件，需同時提供對應的完整可運行原始碼（可透過倉庫連結或附加文件形式）。
- **無擔保條款**  
  作者不對代碼的適用性及穩定性作任何擔保，使用風險由使用者自行承擔。
