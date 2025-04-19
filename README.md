[繁體中文](./README_TW.md) | **简体中文** | [English](./README_EN.md)

# RegexFilter

一个基于 Fabric 的 Minecraft 模组，支持通过正则表达式屏蔽聊天消息，提供基于Cloth
Config API 的可视化配置界面。

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## 依赖项

| 依赖             | 类型 | 版本       | 链接                                                                                                                             |
| ---------------- | ---- | ---------- | -------------------------------------------------------------------------------------------------------------------------------- |
| Minecraft        | 必须 | 1.21       | [Official Website](https://www.minecraft.net/)                                                                                   |
| Fabric Loader    | 必须 | >=0.15.0   | [Github](https://github.com/FabricMC/fabric-loader)                                                                              |
| Fabric API       | 必须 | 任意       | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api) &#124; [Modrinth](https://modrinth.com/mod/fabric-api)     |
| Cloth Config API | 必须 | >=15.0.127 | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cloth-config) &#124; [Modrinth](https://modrinth.com/mod/cloth-config) |
| Mod Menu         | 必须 | >=11.0.0   | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/modmenu) &#124; [Modrinth](https://modrinth.com/mod/modmenu)           |

## 使用指南

1. 安装所有依赖项
2. 进入 Minecraft 游戏主菜单
3. 点击 `模组菜单` → 找到本模组 → 点击mod图标
4. 调整以下设置：
   - `启用模组`：总开关
   - `正则列表`：输入要屏蔽的正则表达式

## 正则表达式示例

| 使用场景                 | 正则表达式              | 说明                                                                                        |
| ------------------------ | ----------------------- | ------------------------------------------------------------------------------------------- |
| 屏蔽特定玩家的消息       | `^\[?\w+\]? >.*`        | 适配 `[玩家名] > 消息` 或 `玩家名: 消息` 格式，需替换 `\w+` 为实际玩家名（如 `^Steve >.*`） |
| 屏蔽含敏感词的消息       | `(?i).*?(作弊\|外挂).*` | 不区分大小写匹配任意位置的敏感词，支持模糊匹配（如"使用外挂"或"作弊者"）                    |
| 屏蔽系统公告类消息       | `^\\[系统\\].*`         | 匹配以 `[系统]` 开头的消息（配置文件需保留双反斜杠转义）                                    |
| 屏蔽带颜色代码的违规消息 | `§[0-9a-fk-or].*违规.*` | 匹配包含颜色/格式代码（如 `§c`）且含"违规"的消息，支持全小写（如 `§c违规警告`）             |

## 开源协议

本项目采用 **[GNU General Public License v3.0](LICENSE)**
协议，您必须遵守以下条款：

- **开源义务**  
  任何基于本项目的修改或衍生作品必须**保持开源**，并使用相同协议（GPL v3.0）发布。
- **协议保留**  
  在分发的副本中必须包含完整的 [LICENSE](LICENSE) 文件及原始版权声明。
- **源代码提供**  
  若分发二进制文件，需同时提供对应的完整可运行源代码（可通过仓库链接或附加文件形式）。
- **无担保条款**  
  作者不对代码的适用性及稳定性作任何担保，使用风险由使用者自行承担。
