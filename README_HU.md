<div align="center">
  <img src="https://github.com/user-attachments/assets/db4982ae-ff09-45ca-9c53-2d764e4acdb9" width="140" height="140" alt="EffectSMP logo"/>
  <h1>⚡ EffectSMP: Reimagined ⚡</h1>
  <p><b>Egyedi képességek, fejlődési rendszer és legendás fegyverek Minecraft szerverekhez.</b></p>

  [![Modrinth](https://img.shields.io/badge/Modrinth-Plugin-00C853?style=for-the-badge&logo=modrinth)](https://modrinth.com/plugin/effectsmp-reimagined)
  [![License](https://img.shields.io/badge/License-Custom-red?style=for-the-badge)](https://github.com/mlnplus/EffectSMP-Reimagined/blob/main/LICENSE)

  <br />

  [ 🇬🇧 **English** ](https://github.com/mlnplus/EffectSMP-Reimagined/blob/main/README.md) &nbsp;•&nbsp; [ 🇭🇺 **Magyar** ](https://github.com/mlnplus/EffectSMP-Reimagined/blob/main/README_HU.md)

  ```
  ⚠️ FIGYELEM: A plugin fejlesztése során használva volt mesterséges intelligencia.
  ```

</div>

---

> [!NOTE]
> Az **EffectSMP: Reimagined** a Minecraft alapértelmezett effektjeit alakítja át egyedi, fejleszthető képességekké, passzív erőkké, szüneteltethető szerverirányítássá és legendás fegyverekké.

---

## 🌟 Főbb Funkciók

- 🔮 **3-Szintes Fejlődési Rendszer**: Gyűjts össze akár 3 Effect Szívet a passzív hatások megerősítéséhez, a cooldownok csökkentéséhez és az aktív képességek feloldásához.
- 🎲 **Ritkasági Alapú Effekt Rendszer**: 11 egyedi effekt **Közönséges**, **Ritka**, **Epikus** és **Legendás** kategóriákba sorolva, testreszabható kezdő sorsolási kategóriákkal.
- 🛡️ **Effektenkénti Cooldown Csökkentés**: Egyedileg beállítható `reduced_cooldown` minden egyes effekthez az `effects.yml`-ben.
- ⚔️ **5 Legendás Egyedi Fegyver**: Egyedi harci mechanikák: **Effect Mace**, **Effect Sword**, **Effect Bow**, **Effect Scythe** és **Effect Spear**.
- ⏸️ **Játék Szüneteltetési & Indítási Rendszer**: Bármikor leállítható és újraindítható játékmechanika, HUD és képességhasználat a `/e pause` / `/e resume` parancsokkal.
- 🌪️ **Széltöltet (Wind Charged) Effekt**: Egyedi mozgási stílus **Végtelen Széltöltet** tárggyal és 2. szinttől teljes esésmentességgel.
- 🎨 **Paper Adventure Sprite Tag-ek**: Hivatalos `<sprite:items:item/...>` és `<head:...>` tag-ek natív támogatása a nyelvi fájlokban és tárgyleírásokban.
- 🖥️ **Interaktív Menük (GUI)**: Böngészhető effektek, 3x3-as interaktív barkácsreceptek, megbízhatósági lista és statisztikák.
- 👥 **Kölcsönös Megbízhatósági Rendszer (Trust)**: Védi a szövetségeseket az egyedi képességek területi sebzésétől.
- 🗄️ **Helyi SQLite Adatbázis**: Alapértelmezett, nagy teljesítményű `database.db` helyi SQLite adatbázis automatikus adatmigrációval.
- 🚫 **100% Duplikáció-mentes Pörgetések**: A Reroll és OP Reroll matematikailag garantálja, hogy sosem kapod meg ugyanazt az effektet, amid épp van.

---

## 🔮 Fejlődési Rendszer & Szívek

A játékosok egy véletlenszerű effekttel és **1 Effect Szívvel** kezdenek. A szívek erősítik a passzív hatásokat, csökkentik a töltési időket és feloldják az aktív képességet:

| Szint | Feltétel | Biztosított Előnyök |
| :--- | :--- | :--- |
| **1. Szint** | ❤️ **1 Szív** | Alapvető passzív effekt, hozzáférés a főmenühöz (`/e`) |
| **2. Szint** | ❤️❤️ **2 Szív** | Megerősített passzív effekt (Amplifier +1), egyedi fegyverek használata |
| **3. Szint** | ❤️❤️❤️ **3 Szív** | **Aktív Képesség Feloldva** (guggolva aktiválható), **Effektenkénti Cooldown Csökkentés** |

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
| 🔄 **Reroll** | Fogyóeszköz | Véletlenszerű **Közönséges** vagy **Ritka** effektet sorsol (sosem duplikál). |
| 🌟 **OP Reroll** | Fogyóeszköz | Véletlenszerű **Epikus** vagy **Legendás** effektet sorsol (sosem duplikál). |
| 🔨 **Effect Mace** | Fegyver (2. Szint+) | A magasba repít; földet éréskor hatalmas lökéshullámot hoz létre. |
| 🗡️ **Effect Sword** | Fegyver (2. Szint+) | Kritikus állapotot aktivál **1.5x sebzéssel és 2x támadási sebességgel** 10 mp-ig. |
| 🏹 **Effect Bow** | Fegyver (2. Szint+) | Átoknyíllal lassító, gyengítő és körvonalazó hatást alkalmaz (10% esély). |
| 🧹 **Effect Scythe** | Fegyver (2. Szint+) | Minden közeli ellenséges játékost teljesen lefagyaszt 5 mp-re. |
| 🔱 **Effect Spear** | Fegyver (2. Szint+) | Tölthető óriási vetődés (lunge), ami nem meríti az éhséget. |

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

### Adminisztrátori & Tesztelői Parancsok

| Parancs | Használat | Jogosultság | Leírás |
| :--- | :--- | :--- | :--- |
| `/e set` | `/e set <effekt> [játékos]` | `effectsmp.admin` / `effectsmp.tester` | Rákényszerít egy effektet a célpontra. |
| `/e give` | `/e give <tárgy> [játékos]` | `effectsmp.admin` / `effectsmp.tester` | Egyedi tárgyakat ad a játékosnak. |
| `/e removecooldown` | `/e rc [típus] [játékos]` | `effectsmp.admin` / `effectsmp.tester` | Törli a tárgy/effekt/dash töltési időket. |
| `/e craftreset` | `/e craftreset [tárgy\|all]` | `effectsmp.admin` / `effectsmp.tester` | Visszaállítja a barkácsolási korlátokat. |
| `/e start` | `/e start` | `effectsmp.admin` / `effectsmp.tester` | Elindítja a játékot és kiadja az effekteket. |
| `/e pause` | `/e pause` / `/e stop` | `effectsmp.admin` / `effectsmp.tester` | Szünetelteti a teljes játékmechanikát és HUD-okat. |
| `/e resume` | `/e resume` | `effectsmp.admin` / `effectsmp.tester` | Újraindítja a szüneteltetett játékmechanikát. |
| `/e reload` | `/e reload` | `effectsmp.admin` | Újratölti a konfigurációs fájlokat. |

---

## ⚙️ Konfigurációs Fájlok

- **`config.yml`**: Adatbázis beállítások (SQLite/MySQL), nyelvválasztás (`en` / `hu`), kezdő sorsolási kategóriák, szív korlátok és Shard drop esélyek.
- **`items.yml`**: Receptek, anyagok, CustomModelData, tárgyak ki/bekapcsolása és fegyver cooldownok módosítása.
- **`effects.yml`**: Effektek ki/bekapcsolása, ritkaságok módosítása, alapértelmezett és effektenkénti `reduced_cooldown` értékek.
- **`messages_en.yml`**: Angol nyelvi állomány.
- **`messages_hu.yml`**: Magyar nyelvi állomány.

---

<div align="center">
  <p style="font-weight: bold;">Védelem alatt: <a href="https://github.com/mlnplus/EffectSMP-Reimagined/blob/main/LICENSE">EffectSMP Public & Attribution License</a></p>
  <p>A kód módosítása saját/szerver célra engedélyezett • Nyilvános újraosztás vagy eladás szigorúan tilos • A készítő (<b>mlnplus</b>) feltüntetése kötelező</p>
  <p>Készítette ❤️-el: <a href="https://github.com/mlnplus">mlnplus</a></p>
</div>
