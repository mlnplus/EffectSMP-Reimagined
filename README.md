<div align="center">
  <img src="https://github.com/user-attachments/assets/db4982ae-ff09-45ca-9c53-2d764e4acdb9" width="160" height="160" alt="EffectSMP logo"/>
  <h1>⚡ EffectSMP: Reimagined ⚡</h1>
  <p><b>The ultimate, feature-packed custom abilities plugin for Spigot/Paper servers.</b></p>

  [![Modrinth](https://img.shields.io/badge/Modrinth-Plugin-00C853?style=for-the-badge&logo=modrinth)](https://modrinth.com/plugin/effectsmp-reimagined)
  [![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://oracle.com/java)
  [![Paper](https://img.shields.io/badge/PaperMC-1.20%20--%201.21-blue?style=for-the-badge)](https://papermc.io)
</div>

---

## 📖 About the Plugin

**EffectSMP: Reimagined** turns default Minecraft potion effects into custom progressable abilities, passive powers, and legendary weapons. 

Players spawn with a random effect and **1 Effect Heart**. By defeating others, crafting, or discovering hidden shards, they can collect up to **3 Effect Hearts**, unlocking deeper passive amplifiers, shorter cooldowns, and devastating sneak-to-cast active abilities!

---

## ✨ Features & Mechanics

### 🔮 Progressive Tier System
- **Level 1 (1 Heart)**: Basic passive potion effect/ability unlocked. Access to the main GUI.
- **Level 2 (2 Hearts)**: Enhanced passive (Amplifier +1) and reduced cooldowns.
- **Level 3 (3 Hearts)**: Unlocks the active ability (sneak-to-activate) and lets players wield custom legendary weapons!

### 💫 Potion Effect Rarity System
Effects are categorized into **4 rarities**, determining their strength and how they can be rolled:

| Rarity | Effects | Roll Source |
| :--- | :--- | :--- |
| **🟢 Common** | Hero of the Village, Fire Resistance, Dolphin Grace | Normal Reroll |
| **🔵 Rare** | Invisibility, Haste, Speed, **Wind Charged** (New!) | Normal Reroll |
| **🟣 Epic** | Health Boost, Regeneration | OP Reroll |
| **🟡 Legendary** | Strength, Resistance | OP Reroll |

---

## 🌪️ New Effect: Wind Charged
The ultimate movement and defense archetype:
- **Level 1**: Grants an **Infinite Wind Charge** item. It is never consumed and does not drop on death!
- **Level 2**: Grants full **Fall Damage Immunity**.
- **Level 3 (Active)**: Sneaking and activating releases a massive wind burst that knocks back all nearby enemies and launches you high into the sky!

---

## 🛠️ Custom Items & Crafting
Discover or craft legendary items to manipulate effects and dominate combat. *All recipes and cooldowns are fully customizable inside `items.yml`!*

- ❤️ **Effect Heart**: Consumed to add +1 Heart. Requires a Shard + Diamond to craft.
- 💎 **Effect Shard**: The core of all custom items. Found inside world loot chests and custom drops:
  - 🏛️ **Ancient City Chests**: 2.5% chance
  - 🚢 **End City Ship Chests**: 5.0% chance
  - 🏡 **Woodland Mansion Chests**: 2.0% chance
  - 👹 **Warden Mobs**: 5.0% drop chance on death
- 🔄 **Reroll**: Changes your active effect to a random Common/Rare effect.
- 🌟 **OP Reroll**: Changes your active effect to a random Epic/Legendary effect.
- ⚔️ **Custom Weapons** (Requires Level 3):
  - **Effect Mace**: Launches you into the air, creating a massive knockback shockwave on landing!
  - **Effect Sword**: Activates a critical state dealing 1.5x damage + 2x attack speed for 10 seconds.
  - **Effect Bow**: Fires explosive, slowing, and weakening curse arrows (10% chance).
  - **Effect Scythe**: Freezes all nearby enemy players completely in place for 5 seconds.
  - **Effect Spear**: Holds to charge a massive, high-velocity Riptide lunge that consumes no hunger!

---

## 🖥️ Interactive GUIs
- `/e` - Main GUI to toggle passives, check cooldowns, and view active stats.
- `/e effects` - Opens the Effects Library GUI detailing the passive and active powers, rarities, and cooldowns of every effect.
- `/e items` - Opens the Custom Items list. Click on any item to view its **interactive 3x3 crafting grid recipe** or check how to find shards!

---

## ⚙️ Configuration Files
This plugin is highly customizable:
- **`config.yml`**: Database setup (YAML/MySQL), basic gameplay options, and auto-update checker.
- **`items.yml`**: Rework crafting recipes, toggling item features, and setting custom cooldown seconds for all weapons.
- **`messages_hu.yml` / `messages_en.yml`**: Modify all item lores, active titles, action-bars, and command error strings.

---

## ⌨️ Command Registry & Permissions

- `/e` - Opens the main menu.
- `/e info` - Displays stats, trusted friends, and active status.
- `/e effects` - Opens the effects list.
- `/e items` - Opens custom items list and recipes.
- `/e trust <player>` - Trust a player (disables friendly fire from custom abilities).
- `/e untrust <player>` - Remove a player from trust list.
- `/e withdraw [amount]` - Convert your current Hearts into physical items to trade.
- `/e activate` - Trigger your active effect ability.

**Admin Commands** (Requires `effectsmp.admin` or `effectsmp.teszter`):
- `/e give <item>` - Spawn custom items.
- `/e set <player> <effect>` - Force set a player's effect.
- `/e removecooldown <player> <item/effect/all>` - Clear active cooldowns.
- `/e craftreset <item/all>` - Reset crafting restrictions.
- `/e reload` - Reload all configs and items.

---


<div align="center">
  <p style="color:red">MODIFICATION AND DISTRIBUTION OF THE PLUGIN IS NOT ALLOWED!</p>
  <p>Made with ❤️ by <a href="https://github.com/mlnplus">mlnplus</a></p>
</div>
