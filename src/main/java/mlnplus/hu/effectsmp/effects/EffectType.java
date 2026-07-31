package mlnplus.hu.effectsmp.effects;

import org.bukkit.potion.PotionEffectType;

public enum EffectType {
    INVISIBILITY("Invisibility", "§7✧ §fInvisibility", PotionEffectType.INVISIBILITY, Rarity.RARE, 600, 0),
    HERO_OF_VILLAGE("Hero of the Village", "§6✦ §fHero of the Village", PotionEffectType.HERO_OF_THE_VILLAGE, Rarity.COMMON, 600, 0),
    HASTE("Haste", "§e⚡ §fHaste", PotionEffectType.HASTE, Rarity.RARE, 300, 0),
    FIRE_RESISTANCE("Fire Resistance", "§c🔥 §fFire Resistance", PotionEffectType.FIRE_RESISTANCE, Rarity.COMMON, 900, 0),
    SPEED("Speed", "§b➣ §fSpeed", PotionEffectType.SPEED, Rarity.RARE, 300, 0),
    DOLPHIN_GRACE("Dolphin Grace", "§3🌊 §fDolphin Grace", PotionEffectType.DOLPHINS_GRACE, Rarity.COMMON, 300, 0),
    HEALTH_BOOST("Health Boost", "§c❤ §fHealth Boost", PotionEffectType.HEALTH_BOOST, Rarity.EPIC, 1200, 0),
    WIND_CHARGED("Wind Charged", "§b⚡ §fWind Charged", null, Rarity.RARE, 180, 0),

    RESISTANCE("Resistance", "§9⛊ §9Resistance", PotionEffectType.RESISTANCE, Rarity.LEGENDARY, 900, 0),
    STRENGTH("Strength", "§4⚔ §4Strength", PotionEffectType.STRENGTH, Rarity.LEGENDARY, 900, 0),
    REGENERATION("Regeneration", "§c❤ §cRegeneration", PotionEffectType.REGENERATION, Rarity.EPIC, 900, 0);

    public enum Rarity {
        COMMON("§8[§7Common§8]", "common"),
        RARE("§8[§bRare§8]", "rare"),
        EPIC("§8[§dEpic§8]", "epic"),
        LEGENDARY("§8[§6Legendary§8]", "legendary");

        private final String displayName;
        private final String key;

        Rarity(String displayName, String key) {
            this.displayName = displayName;
            this.key = key;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getKey() {
            return key;
        }
    }

    private final String name;
    private final String displayName;
    private final PotionEffectType potionEffect;
    private final Rarity rarity;
    private final int cooldownSeconds;
    private final int passiveAmplifier;

    EffectType(String name, String displayName, PotionEffectType potionEffect, Rarity rarity, int cooldownSeconds,
            int passiveAmplifier) {
        this.name = name;
        this.displayName = displayName;
        this.potionEffect = potionEffect;
        this.rarity = rarity;
        this.cooldownSeconds = cooldownSeconds;
        this.passiveAmplifier = passiveAmplifier;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PotionEffectType getPotionEffect() {
        return potionEffect;
    }

    public boolean isOP() {
        return rarity == Rarity.EPIC || rarity == Rarity.LEGENDARY;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public int getPassiveAmplifier() {
        return passiveAmplifier;
    }

    public static EffectType fromString(String name) {
        for (EffectType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public static EffectType[] getNormalEffects() {
        java.util.List<EffectType> list = new java.util.ArrayList<>();
        for (EffectType type : values()) {
            if (type.getRarity() == Rarity.COMMON || type.getRarity() == Rarity.RARE) {
                list.add(type);
            }
        }
        return list.toArray(new EffectType[0]);
    }

    public static EffectType[] getOPEffects() {
        java.util.List<EffectType> list = new java.util.ArrayList<>();
        for (EffectType type : values()) {
            if (type.getRarity() == Rarity.EPIC || type.getRarity() == Rarity.LEGENDARY) {
                list.add(type);
            }
        }
        return list.toArray(new EffectType[0]);
    }
}
