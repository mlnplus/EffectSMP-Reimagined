<div align="center">
  <img src="https://github.com/user-attachments/assets/db4982ae-ff09-45ca-9c53-2d764e4acdb9" width="140" height="140" alt="EffectSMP logo"/>
  <h1>⚡ EffectSMP: Reimagined ⚡</h1>
  <p><b>Custom abilities, progressive heart mechanics, and legendary weapons for Minecraft servers.</b></p>

  [![Modrinth](https://img.shields.io/badge/Modrinth-Plugin-00C853?style=for-the-badge&logo=modrinth)](https://modrinth.com/plugin/effectsmp-reimagined)
  [![PaperMC](https://img.shields.io/badge/PaperMC-1.20%20--%201.21.x-blue?style=for-the-badge&logo=papermc)](https://papermc.io)
  [![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://oracle.com/java)

  <br />

  [ 🇬🇧 **English** ](README.md) &nbsp;•&nbsp; [ 🇭🇺 **Magyar** ](README_HU.md)

  ```
  ⚠️ DISCLAIMER: Atrificial Intelligence was used in the development of this plugin.
  ```

</div>

---

> [!NOTE]
> **EffectSMP: Reimagined** elevates standard Minecraft potion effects into custom abilities, progressive heart power-ups, and game-changing legendary items.

---

## 🌟 Key Features

- 🔮 **3-Tier Progression System**: Collect up to 3 Effect Hearts to boost passives, reduce cooldowns, and unlock active abilities.
- 🎲 **Rarity-Based Effect System**: 11 unique abilities categorized across **Common**, **Rare**, **Epic**, and **Legendary** tiers.
- ⚔️ **5 Custom Legendary Weapons**: Unique combat mechanics including **Effect Mace**, **Effect Sword**, **Effect Bow**, **Effect Scythe**, and **Effect Spear**.
- 🌪️ **Wind Charged Archetype**: Custom movement archetype featuring an **Infinite Wind Charge** and complete fall immunity at Level 2+.
- 🖥️ **Interactive In-Game GUIs**: Browse abilities, inspect interactive 3x3 crafting recipes, view trust status, and check detailed player stats.
- 👥 **Mutual Trust System**: Protect teammates from friendly-fire AOE abilities.
- 💾 **Dual Storage Options**: Local YAML storage or high-performance MySQL integration.

---

## 🔮 Progression & Heart Mechanics

Players begin with a random effect and **1 Effect Heart**. Hearts power up passives, decrease cooldowns, and grant access to active abilities:

| Level | Requirement | Granted Benefits |
| :--- | :--- | :--- |
| **Level 1** | ❤️ **1 Heart** | Basic passive effect, access to main GUI (`/e`) |
| **Level 2** | ❤️❤️ **2 Hearts** | Enhanced passive effect (Amplifier +1), ability to wield custom weapons |
| **Level 3** | ❤️❤️❤️ **3 Hearts** | **Active Ability Unlocked** (sneak-to-activate), **25% Cooldown Reduction** |

> [!IMPORTANT]
> If a player loses all Effect Hearts, their passive effect is disabled until they acquire a new Heart.

---

## 💫 Effects & Rarities

| Rarity | Effect | Type | Key Ability / Benefit |
| :--- | :--- | :--- | :--- |
| **🟢 Common** | **Hero of the Village** | Passive / Active | Villager discounts / Hero V surge (2 min) |
| **🟢 Common** | **Fire Resistance** | Passive / Active | Fire immunity / Ignites surrounding enemies (15s) |
| **🟢 Common** | **Dolphin Grace** | Passive / Active | Swim speed boost / Water Breathing & Conduit Power (1 min) |
| **🔵 Rare** | **Invisibility** | Passive / Active | True invisibility / Hide from non-trusted players (10s) |
| **🔵 Rare** | **Haste** | Passive / Active | Mining speed / Unlocks **3x3 Area Mining** (30s) |
| **🔵 Rare** | **Speed** | Passive / Active | Speed I / **3x Charge Dash Ability** |
| **🔵 Rare** | **Wind Charged** | Passive / Active | Infinite Wind Charge & Fall Immunity / Massive Wind Burst |
| **🟣 Epic** | **Health Boost** | Passive / Active | Extra hearts / **+10 Bonus Hearts Surge** (30s) |
| **🟣 Epic** | **Regeneration** | Passive / Active | Constant regen / Shares Regeneration II with trusted friends (45s) |
| **🟡 Legendary** | **Strength** | Passive / Active | Melee boost / Strength III & **+3 Block Reach** (15s) |
| **🟡 Legendary** | **Resistance** | Passive / Active | Damage reduction / **FULL INVULNERABILITY** (20s) |

---

## ⚔️ Custom Weapons & Crafting

Craft or discover custom items to manipulate abilities and dominate combat. *All recipes and cooldowns are fully configurable in `items.yml`.*

| Item | Type | Special Ability & Mechanics |
| :--- | :--- | :--- |
| ❤️ **Effect Heart** | Consumable | Adds +1 Heart (Max 3). Crafted with Shards and Netherite/Diamonds. |
| 💎 **Effect Shard** | Ingredient | Crafting core for custom items. Found in world loot & Warden drops. |
| 🔄 **Reroll** | Consumable | Rolls a random **Common** or **Rare** effect. |
| 🌟 **OP Reroll** | Consumable | Rolls a random **Epic** or **Legendary** effect. |
| 🔨 **Effect Mace** | Weapon (Lvl 3) | Launches user high into the air; creates a massive knockback shockwave on landing. |
| 🗡️ **Effect Sword** | Weapon (Lvl 3) | Activates critical state dealing **1.5x damage + 2x attack speed** for 10s. |
| 🏹 **Effect Bow** | Weapon (Lvl 3) | Fires curse arrows that apply Slowness, Weakness, and Glowing (10% chance). |
| 🧹 **Effect Scythe** | Weapon (Lvl 3) | Freezes all nearby enemy players completely in place for 5s. |
| 🔱 **Effect Spear** | Weapon (Lvl 3) | Charge a high-velocity Riptide lunge that consumes zero hunger. |

### 🏛️ Shard Obtain Locations

| Location / Source | Chance |
| :--- | :--- |
| 🏛️ **Ancient City Chests** | **7.5%** |
| 🚢 **End City Ship Chests** | **7.5%** |
| 🏡 **Woodland Mansion Chests** | **7.5%** |
| 👹 **Warden Mob Drop** | **15.0%** |

---

## ⌨️ Command Registry & Permissions

### Player Commands

| Command | Usage | Description |
| :--- | :--- | :--- |
| `/e` | `/e` | Opens the Main GUI menu. |
| `/e info` | `/e info` | Displays player stats, trusted friends, and active status. |
| `/e effects` | `/e effects` | Opens the Effects Library GUI. |
| `/e items` | `/e items` | Opens Custom Items list with interactive recipes. |
| `/e activate` | `/e activate` | Triggers active effect ability. |
| `/e trust` | `/e trust <player>` | Add player to mutual trust list (disables ability friendly fire). |
| `/e untrust` | `/e untrust <player>` | Remove player from trust list. |
| `/e withdraw` | `/e withdraw [amount]` | Convert physical Hearts into item form. |

### Admin Commands

| Command | Usage | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/e set` | `/e set <effect> [player]` | `effectsmp.admin` | Force set a player's effect. |
| `/e give` | `/e give <item> [player]` | `effectsmp.admin` | Spawn custom items. |
| `/e removecooldown` | `/e removecooldown [type] [player]` | `effectsmp.admin` | Reset item/effect cooldowns. |
| `/e craftreset` | `/e craftreset [item\|all]` | `effectsmp.admin` | Reset limited crafting limits. |
| `/e start` | `/e start` | `effectsmp.admin` | Start game and assign initial effects. |
| `/e reload` | `/e reload` | `effectsmp.admin` | Reload configuration files. |

---

## ⚙️ Configuration Files

- **`config.yml`**: Database setup (YAML/MySQL), language setting (`en` / `hu`), and auto-updater.
- **`items.yml`**: Customize recipes, toggle items, and adjust weapon cooldown seconds.
- **`messages_en.yml`**: Full English localization (action-bars, titles, GUI names, error strings).
- **`messages_hu.yml`**: Full Hungarian localization option.

---

<div align="center">
  <p style="color: red; font-weight: bold;">MODIFICATION AND REDISTRIBUTION IS NOT PERMITTED</p>
  <p>Created with ❤️ by <a href="https://github.com/mlnplus">mlnplus</a></p>
</div>
