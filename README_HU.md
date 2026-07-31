<div align="center">
  <img src="https://github.com/user-attachments/assets/db4982ae-ff09-45ca-9c53-2d764e4acdb9" width="140" height="140" alt="EffectSMP logo"/>
  <h1>⚡ EffectSMP: Reimagined ⚡</h1>
  <p><b>Egyedi képességek, fejlődési rendszer és legendás fegyverek Minecraft szerverekhez.</b></p>

  [![Modrinth](https://img.shields.io/badge/Modrinth-Plugin-00C853?style=for-the-badge&logo=modrinth)](https://modrinth.com/plugin/effectsmp-reimagined)
  [![PaperMC](https://img.shields.io/badge/PaperMC-1.20%20--%201.21.x-blue?style=for-the-badge&logo=papermc)](https://papermc.io)
  [![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://oracle.com/java)

  <br />

  [ 🇬🇧 **English** ](README.md) &nbsp;•&nbsp; [ 🇭🇺 **Magyar** ](README_HU.md)
</div>

---

> [!NOTE]
> Az **EffectSMP: Reimagined** a Minecraft alapértelmezett effektjeit alakítja át egyedi, fejleszthető képességekké, passzív erökké és legendás fegyverekké.

---

## 🌟 Főbb Funkciók

- 🔮 **3-Szintes Fejlődési Rendszer**: Gyűjts össze akár 3 Effect Szívet a passzív hatások megerősítéséhez, a cooldownok csökkentéséhez és az aktív képességek feloldásához.
- 🎲 **Ritkasági Alapú Effekt Rendszer**: 11 egyedi effekt **Közönséges**, **Ritka**, **Epikus** és **Legendás** kategóriákba sorolva.
- ⚔️ **5 Legendás Egyedi Fegyver**: Egyedi harci mechanikák: **Effect Mace**, **Effect Sword**, **Effect Bow**, **Effect Scythe** és **Effect Spear**.
- 🌪️ **Széltöltet (Wind Charged) Effekt**: Egyedi mozgási stílus **Végtelen Széltöltet** tárggyal és 2. szinttől teljes esésmentességgel.
- 🖥️ **Interaktív Menük (GUI)**: Böngészhető effektek, 3x3-as interaktív barkácsreceptek, megbízhatósági lista és statisztikák.
- 👥 **Kölcsönös Megbízhatósági Rendszer (Trust)**: Védi a szövetségeseket az egyedi képességek területi sebzésétől.
- 💾 **Rugalmas Adattárolás**: Helyi YAML fájlok vagy nagy teljesítményű MySQL adatbázis támogatás.

---

## 🔮 Fejlődési Rendszer & Szívek

A játékosok egy véletlenszerű effekttel és **1 Effect Szívvel** kezdenek. A szívek erősítik a passzív hatásokat, csökkentik a töltési időket és feloldják az aktív képességet:

| Szint | Feltétel | Biztosított Előnyök |
| :--- | :--- | :--- |
| **1. Szint** | ❤️ **1 Szív** | Alapvető passzív effekt, hozzáférés a főmenühöz (`/e`) |
| **2. Szint** | ❤️❤️ **2 Szív** | Megerősített passzív effekt (Amplifier +1), egyedi fegyverek használata |
| **3. Szint** | ❤️❤️❤️ **3 Szív** | **Aktív Képesség Feloldva** (guggolva aktiválható), **25% Cooldown Csökkentés** |

> [!IMPORTANT]
> Ha egy játékos elveszíti az összes Effect Szívét, a passzív effekte letiltásra kerül, amíg nem szerez újabb Szívet.

---

## 💫 Effektek és Ritkaságok

| Ritkaság | Effekt | Típus | Fő Képesség / Előny |
| :--- | :--- | :--- | :--- |
| **🟢 Közönséges** | **Hero of the Village** | Passzív / Aktív | Falusi kedvezmények / Hero V roham (2 perc) |
| **🟢 Közönséges** | **Fire Resistance** | Passzív / Aktív | Tűzimmunitás / Meggyújtja a közeli ellenségeket (15 mp) |
| **🟢 Közönséges** | **Dolphin Grace** | Passzív / Aktív | Úszási sebesség / Víz alatti légzés & Conduit Power (1 perc) |
| **🔵 Ritka** | **Invisibility** | Passzív / Aktív | Láthatatlanság / Elrejt a nem megbízott játékosok elől (10 mp) |
| **🔵 Ritka** | **Haste** | Passzív / Aktív | Bányászati sebesség / **3x3-as Területi Bányászás** (30 mp) |
| **🔵 Ritka** | **Speed** | Passzív / Aktív | Gyorsaság I / **3x Töltésű Lökés (Dash) Képesség** |
| **🔵 Ritka** | **Wind Charged** | Passzív / Aktív | Végtelen Széltöltet & Esési Immunitás / Hatalmas Széllökés |
| **🟣 Epikus** | **Health Boost** | Passzív / Aktív | Extra szívek / **+10 Bónusz Szív Roham** (30 mp) |
| **🟣 Epikus** | **Regeneration** | Passzív / Aktív | Folyamatos regen / Regeneráció II megosztása megbízott társaiddal (45 mp) |
| **🟡 Legendás** | **Strength** | Passzív / Aktív | Sebzésbónusz / Erő III & **+3 Blokk Hatótávolság** (15 mp) |
| **🟡 Legendás** | **Resistance** | Passzív / Aktív | Sebzéscsökkentés / **TELJES SEBZHETETLENSÉG** (20 mp) |

---

## ⚔️ Egyedi Tárgyak és Barkácsolás

Készíts vagy keress egyedi tárgyakat a képességek irányításához. *Minden recept és töltési idő testreszabható az `items.yml`-ben.*

| Tárgy | Típus | Képesség & Mechanika |
| :--- | :--- | :--- |
| ❤️ **Effect Szív** | Fogyóeszköz | +1 Szívet ad (Max 3). Shardból és Gyémántból/Netherite-ből barkácsolható. |
| 💎 **Effect Shard** | Alapanyag | Minden egyedi tárgy alapja. Láda zsákmányokban és Warden dropként található. |
| 🔄 **Reroll** | Fogyóeszköz | Véletlenszerű **Közönséges** vagy **Ritka** effektet sorsol. |
| 🌟 **OP Reroll** | Fogyóeszköz | Véletlenszerű **Epikus** vagy **Legendás** effektet sorsol. |
| 🔨 **Effect Mace** | Fegyver (3. Szint) | A magasba repít; földet éréskor hatalmas lökéshullámot hoz létre. |
| 🗡️ **Effect Sword** | Fegyver (3. Szint) | Kritikus állapotot aktivál **1.5x sebzéssel és 2x támadási sebességgel** 10 mp-ig. |
| 🏹 **Effect Bow** | Fegyver (3. Szint) | Átoknyíllal lassító, gyengítő és körvonalazó hatást alkalmaz (10% esély). |
| 🧹 **Effect Scythe** | Fegyver (3. Szint) | Minden közeli ellenséges játékost teljesen lefagyaszt 5 mp-re. |
| 🔱 **Effect Spear** | Fegyver (3. Szint) | Tölthető óriási vetődés (lunge), ami nem meríti az éhséget. |

### 🏛️ Shard Megtalálási Helyek

| Helyszín / Forrás | Esély |
| :--- | :--- |
| 🏛️ **Ősi Város Láda (Ancient City)** | **7.5%** |
| 🚢 **Végváros Hajó Láda (End City Ship)** | **7.5%** |
| 🏡 **Erdei Kastély Láda (Woodland Mansion)** | **7.5%** |
| 👹 **Warden Szörny Drop** | **15.0%** |

---

## ⌨️ Parancsok és Jogosultságok

### Játékos Parancsok

| Parancs | Használat | Leírás |
| :--- | :--- | :--- |
| `/e` | `/e` | Megnyitja a Fő GUI menüt. |
| `/e info` | `/e info` | Megjeleníti a saját statisztikáidat és a megbízott játékosokat. |
| `/e effects` | `/e effects` | Megnyitja az Effekt Könyvtárat. |
| `/e items` | `/e items` | Megnyitja az Egyedi Tárgyak és receptek listáját. |
| `/e activate` | `/e activate` | Aktiválja az aktív effekt képességet. |
| `/e trust` | `/e trust <játékos>` | Megbízhatóvá tesz egy játékost (letiltja a területi sebzést rá). |
| `/e untrust` | `/e untrust <játékos>` | Eltávolít egy játékost a megbízhatósági listáról. |
| `/e withdraw` | `/e withdraw [mennyiség]` | Fizikai tárggyá alakítja az Effect Szíveidet a kereskedéshez. |

### Adminisztrátori Parancsok

| Parancs | Használat | Jogosultság | Leírás |
| :--- | :--- | :--- | :--- |
| `/e set` | `/e set <effekt> [játékos]` | `effectsmp.admin` | Rákényszerít egy effektet a célpontra. |
| `/e give` | `/e give <tárgy> [játékos]` | `effectsmp.admin` | Egyedi tárgyakat ad a játékosnak. |
| `/e removecooldown` | `/e removecooldown [típus] [játékos]` | `effectsmp.admin` | Törli a tárgy/effekt töltési időket. |
| `/e craftreset` | `/e craftreset [tárgy\|all]` | `effectsmp.admin` | Visszaállítja a barkácsolási korlátokat. |
| `/e start` | `/e start` | `effectsmp.admin` | Elindítja a játékot és kiadja az effekteket. |
| `/e reload` | `/e reload` | `effectsmp.admin` | Újratölti a konfigurációs fájlokat. |

---

## ⚙️ Konfigurációs Fájlok

- **`config.yml`**: Adatbázis beállítások (YAML/MySQL), nyelvválasztás (`en` / `hu`) és frissítés-ellenőrző.
- **`items.yml`**: Receptek, tárgyak ki/bekapcsolása és fegyver cooldownok módosítása.
- **`messages_en.yml`**: Angol nyelvi állomány.
- **`messages_hu.yml`**: Magyar nyelvi állomány.

---

<div align="center">
  <p style="color: red; font-weight: bold;">MÓDOSÍTÁS ÉS ÚJRAOSZTÁS NEM ENGEDÉLYEZETT</p>
  <p>Készítette ❤️-el: <a href="https://github.com/mlnplus">mlnplus</a></p>
</div>
