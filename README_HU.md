<div align="center">
  <img src="https://github.com/user-attachments/assets/db4982ae-ff09-45ca-9c53-2d764e4acdb9" width="160" height="160" alt="EffectSMP logo"/>
  <h1>⚡ EffectSMP: Reimagined (HU) ⚡</h1>
  <p><b>A legteljesebb, egyedi képességekkel és fegyverekkel teli Spigot/Paper szerver plugin.</b></p>

  [![Modrinth](https://img.shields.io/badge/Modrinth-Plugin-00C853?style=for-the-badge&logo=modrinth)](https://modrinth.com/plugin/effectsmp-reimagined)
  [![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://oracle.com/java)
  [![Paper](https://img.shields.io/badge/PaperMC-1.20%20--%201.21-blue?style=for-the-badge)](https://papermc.io)
</div>

---

## 📖 A pluginról

Az **EffectSMP: Reimagined** az alapértelmezett Minecraft effekteket alakítja át fejleszthető egyedi képességekké, passzív erökké és legendás fegyverekké.

A játékosok egy véletlenszerű effekttel és **1 Effect Szívvel** kezdik a játékot. Mások legyőzésével, tárgyak barkácsolásával vagy elrejtett shardok felkutatásával akár **3 Effect Szívet** is összegyűjthetnek, ami erősebb passzív hatásokat, rövidebb töltési időket (cooldown) és pusztító aktív képességeket nyit meg!

---

## ✨ Funkciók és Mechanikák

### 🔮 Fejlődési Rendszer
- **1. Szint (1 Szív)**: Alapvető passzív effekt feloldva. Hozzáférés a fő GUI-hoz.
- **2. Szint (2 Szív)**: Megerősített passzív hatás (Amplifier +1) és csökkentett töltési idők.
- **3. Szint (3 Szív)**: Feloldja az aktív képességet (guggolva aktiválható) és képessé teszi a játékost a legendás fegyverek használatára!

### 💫 Effektek Ritkasági Rendszere
Az effektek **4 ritkasági szintbe** vannak sorolva, ami meghatározza erejüket és azt, hogy hogyan lehet őket pörgetni:

| Ritkaság | Effektek | Pörgetési Forrás |
| :--- | :--- | :--- |
| **🟢 Közönséges** | Hero of the Village, Fire Resistance, Dolphin Grace | Sima Reroll |
| **🔵 Ritka** | Invisibility, Haste, Speed, **Wind Charged** (Új!) | Sima Reroll |
| **🟣 Epikus** | Health Boost, Regeneration | OP Reroll |
| **🟡 Legendás** | Strength, Resistance | OP Reroll |

---

## 🌪️ Új Effekt: Wind Charged (Széltöltet)
A legdinamikusabb mozgási és védelmi stílus:
- **1. Szint**: Egy **Végtelen Széltöltet** tárgyat ad. Soha nem fogy el használatkor, és halálkor sem esik ki!
- **2. Szint**: Teljes **Esési Sebzés Immunitást** biztosít.
- **3. Szint (Aktív)**: Guggolás közben aktiválva hatalmas szélrobbanást idéz elő, ami minden közeli ellenséget hátralök, téged pedig magasra repít a levegőbe!

---

## 🛠️ Egyedi Tárgyak és Barkácsolás
Készíts vagy keress legendás tárgyakat a képességek manipulálásához és a harcok irányításához. *Minden recept és cooldown teljesen testreszabható az `items.yml`-ben!*

- ❤️ **Effect Szív (Heart)**: Elhasználva +1 Szívet ad. Shardból és Gyémántból készíthető.
- 💎 **Effect Shard**: Minden egyedi tárgy alapja. Világszerte megtalálható ládákban és mob kiejtésekben:
  - 🏛️ **Ősi Város Láda (Ancient City)**: 2.5% esély
  - 🚢 **Végváros Hajó Láda (End City Ship)**: 5.0% esély
  - 🏡 **Erdei Kastély Láda (Woodland Mansion)**: 2.0% esély
  - 👹 **Warden Szörnyek**: 5.0% kiejtési esély halálkor
- 🔄 **Reroll**: Megváltoztatja az aktív effekted egy véletlenszerű Közönséges/Ritka effektre.
- 🌟 **OP Reroll**: Megváltoztatja az aktív effekted egy véletlenszerű Epikus/Legendás effektre.
- ⚔️ **Egyedi Fegyverek** (3. Szint szükséges):
  - **Effect Mace**: A magasba lök, földet éréskor pedig hatalmas lökéshullámot hoz létre!
  - **Effect Sword**: 10 másodpercre 1.5x sebzést és 2x támadási sebességet ad minden ütésre.
  - **Effect Bow**: Robbantó, lassító és gyengítő átok nyilakat lő (10% esély).
  - **Effect Scythe**: Minden közeli ellenséges játékost 5 másodpercre teljesen lefagyaszt.
  - **Effect Spear**: Hosszan nyomva tartva tölthető óriási vetődéshez (lunge), ami nem meríti az éhséget!

---

## 🖥️ Interaktív GUI-k
- `/e` - Fő GUI a passzívok ki/bekapcsolásához, statisztikák és cooldownok megtekintéséhez.
- `/e effects` - Megnyitja az Effekt Könyvtár menüt, ahol látható az összes effekt passzív/aktív leírása, ritkasága és töltési ideje.
- `/e items` - Megjeleníti az egyedi tárgyak listáját. Bármelyik tárgyra kattintva látható az **interaktív 3x3-as barkács receptje**, vagy leírás a shardok megtalálásához!

---

## ⚙️ Konfigurációs Fájlok
A plugin szinte minden része testreszabható:
- **`config.yml`**: Adatbázis beállítások (YAML/MySQL), alapvető játékbeállítások és automatikus frissítés-ellenőrző.
- **`items.yml`**: Barkácsreceptek átrendezése, tárgyak ki/bekapcsolása és a fegyverek egyedi másodperces cooldownjainak módosítása.
- **`messages_hu.yml` / `messages_en.yml`**: Minden tárgyleírás, aktív címek, akcióbárok és hibaüzenetek nyelvi szerkesztése.

---

## ⌨️ Parancsok és Jogosultságok

- `/e` - Megnyitja a főmenüt.
- `/e info` - Megjeleníti a saját statisztikáidat, trustolt játékosokat és az aktív státuszt.
- `/e effects` - Megnyitja az effektek leírását tartalmazó listát.
- `/e items` - Megnyitja a tárgyak és receptek listáját.
- `/e trust <játékos>` - Megbízol egy játékosban (így nem sebzik őt az egyedi képességeid).
- `/e untrust <játékos>` - Eltávolítasz egy játékost a megbízhatósági listáról.
- `/e withdraw [mennyiség]` - Fizikai tárggyá alakítja az Effect Szíveidet a kereskedéshez.
- `/e activate` - Aktiválja az aktív effekt képességedet.

**Adminisztrátori Parancsok** (`effectsmp.admin` vagy `effectsmp.teszter` szükséges):
- `/e give <tárgy>` - Egyedi tárgyakat ad a játékosnak.
- `/e set <játékos> <effekt>` - Rákényszerít egy effektet a célpontra.
- `/e removecooldown <játékos> <item/effect/all>` - Törli a cooldownokat.
- `/e craftreset <tárgy/all>` - Visszaállítja a limitált fegyverek barkácsolási korlátját.
- `/e reload` - Újratölti a konfigurációkat és az egyedi tárgyakat.

---

<div align="center">
  <p>Készítette ❤️-el: <a href="https://github.com/mlnplus">mlnplus</a></p>
</div>
