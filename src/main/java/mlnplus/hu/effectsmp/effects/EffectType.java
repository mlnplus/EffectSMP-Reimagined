package mlnplus.hu.effectsmp.effects;

import org.bukkit.potion.PotionEffectType;

public enum EffectType {
    // Normal Effects
    INVISIBILITY("Invisibility", "§7✧ §fInvisibility", PotionEffectType.INVISIBILITY, false, 600, 1),
    HERO_OF_VILLAGE("Hero of the Village", "§6✦ §fHero of the Village", PotionEffectType.HERO_OF_THE_VILLAGE, false,
            600, 1),
    HASTE("Haste", "§e⚡ §fHaste", PotionEffectType.HASTE, false, 300, 1),
    FIRE_RESISTANCE("Fire Resistance", "§c🔥 §fFire Resistance", PotionEffectType.FIRE_RESISTANCE, false, 900, 1),
    SPEED("Speed", "§b➣ §fSpeed", PotionEffectType.SPEED, false, 300, 1),
    DOLPHIN_GRACE("Dolphin Grace", "§3🌊 §fDolphin Grace", PotionEffectType.DOLPHINS_GRACE, false, 300, 1),
    HEALTH_BOOST("Health Boost", "§c❤ §fHealth Boost", PotionEffectType.HEALTH_BOOST, false, 1200, 1),

    // OP Effects (gold/orange theme)
    RESISTANCE("Resistance", "§9⛊ §9Resistance", PotionEffectType.RESISTANCE, true, 900, 1),
    STRENGTH("Strength", "§4⚔ §4Strength", PotionEffectType.STRENGTH, true, 900, 1),
    REGENERATION("Regeneration", "§c❤ §cRegeneration", PotionEffectType.REGENERATION, true, 900, 1);

    private final String name;
    private final String displayName;
    private final PotionEffectType potionEffect;
    private final boolean isOP;
    private final int cooldownSeconds;
    private final int passiveAmplifier;

    EffectType(String name, String displayName, PotionEffectType potionEffect, boolean isOP, int cooldownSeconds,
            int passiveAmplifier) {
        this.name = name;
        this.displayName = displayName;
        this.potionEffect = potionEffect;
        this.isOP = isOP;
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
        return isOP;
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
        return new EffectType[] { INVISIBILITY, HERO_OF_VILLAGE, HASTE, FIRE_RESISTANCE, SPEED, DOLPHIN_GRACE,
                HEALTH_BOOST };
    }

    public static EffectType[] getOPEffects() {
        return new EffectType[] { RESISTANCE, STRENGTH, REGENERATION };
    }
}
