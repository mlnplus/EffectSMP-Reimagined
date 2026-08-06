package mlnplus.hu.effectsmp.effects;

import mlnplus.hu.effectsmp.Effectsmp;
import org.bukkit.potion.PotionEffectType;

public enum EffectType {
    INVISIBILITY("Invisibility", PotionEffectType.INVISIBILITY, Rarity.RARE, 600, 0),
    HERO_OF_VILLAGE("Hero of the Village", PotionEffectType.HERO_OF_THE_VILLAGE, Rarity.COMMON, 600, 0),
    HASTE("Haste", PotionEffectType.HASTE, Rarity.RARE, 300, 0),
    FIRE_RESISTANCE("Fire Resistance", PotionEffectType.FIRE_RESISTANCE, Rarity.COMMON, 900, 0),
    SPEED("Speed", PotionEffectType.SPEED, Rarity.RARE, 300, 0),
    DOLPHIN_GRACE("Dolphin Grace", PotionEffectType.DOLPHINS_GRACE, Rarity.COMMON, 300, 0),
    HEALTH_BOOST("Health Boost", PotionEffectType.HEALTH_BOOST, Rarity.EPIC, 300, 0),
    WIND_CHARGED("Wind Charged", null, Rarity.RARE, 180, 0),

    RESISTANCE("Resistance", PotionEffectType.RESISTANCE, Rarity.LEGENDARY, 900, 0),
    STRENGTH("Strength", PotionEffectType.STRENGTH, Rarity.LEGENDARY, 900, 0),
    REGENERATION("Regeneration", PotionEffectType.REGENERATION, Rarity.EPIC, 900, 0);

    public enum Rarity {
        COMMON("Common", "common"),
        RARE("Rare", "rare"),
        EPIC("Epic", "epic"),
        LEGENDARY("Legendary", "legendary");

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
    private final PotionEffectType potionEffect;
    private final Rarity rarity;
    private final int cooldownSeconds;
    private final int passiveAmplifier;

    EffectType(String name, PotionEffectType potionEffect, Rarity rarity, int cooldownSeconds,
            int passiveAmplifier) {
        this.name = name;
        this.potionEffect = potionEffect;
        this.rarity = rarity;
        this.cooldownSeconds = cooldownSeconds;
        this.passiveAmplifier = passiveAmplifier;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return getGradientName();
    }

    public String getPlainDisplayName() {
        return name;
    }

    public String getGradientName() {
        return switch (this) {
            case INVISIBILITY -> "<gradient:#8E9EAB:#EEF2F3><!b>✧</!b> <b>Invisibility</b></gradient>";
            case HERO_OF_VILLAGE -> "<gradient:#11998E:#38EF7D><!b>❇</!b> <b>Hero of the Village</b></gradient>";
            case HASTE -> "<gradient:#F7971E:#FFD200><!b>⚡</!b> <b>Haste</b></gradient>";
            case FIRE_RESISTANCE -> "<gradient:#FF416C:#FF4B2B><!b>🔥</!b> <b>Fire Resistance</b></gradient>";
            case SPEED -> "<gradient:#00C9FF:#92FE9D><!b>➣</!b> <b>Speed</b></gradient>";
            case DOLPHIN_GRACE -> "<gradient:#00B4DB:#0083B0><!b>🌊</!b> <b>Dolphin Grace</b></gradient>";
            case HEALTH_BOOST -> "<gradient:#FF4D4D:#F9CB43><!b>❤</!b> <b>Health Boost</b></gradient>";
            case WIND_CHARGED -> "<gradient:#7F00FF:#E100FF><!b>🌀</!b> <b>Wind Charged</b></gradient>";
            case RESISTANCE -> "<gradient:#4B6CB7:#182E6E><!b>⛊</!b> <b>Resistance</b></gradient>";
            case STRENGTH -> "<gradient:#ED213A:#93291E><!b>⚔</!b> <b>Strength</b></gradient>";
            case REGENERATION -> "<gradient:#FF0844:#FFB199><!b>💖</!b> <b>Regeneration</b></gradient>";
        };
    }

    public PotionEffectType getPotionEffect() {
        return potionEffect;
    }

    public boolean isEnabled() {
        Effectsmp instance = Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getEffectsConfig() != null) {
            return instance.getConfigManager().getEffectsConfig().getBoolean(getConfigKey() + ".enabled", true);
        }
        return true;
    }

    public boolean isOP() {
        Rarity r = getRarity();
        return r == Rarity.EPIC || r == Rarity.LEGENDARY;
    }

    public Rarity getRarity() {
        Effectsmp instance = Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getEffectsConfig() != null) {
            String rStr = instance.getConfigManager().getEffectsConfig().getString(getConfigKey() + ".rarity");
            if (rStr != null && !rStr.trim().isEmpty()) {
                for (Rarity r : Rarity.values()) {
                    if (r.name().equalsIgnoreCase(rStr.trim()) || r.getKey().equalsIgnoreCase(rStr.trim())) {
                        return r;
                    }
                }
            }
        }
        return rarity;
    }

    public String getConfigKey() {
        return name().toLowerCase();
    }

    public int getCooldownSeconds() {
        Effectsmp instance = Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getEffectsConfig() != null) {
            return instance.getConfigManager().getEffectsConfig().getInt(getConfigKey() + ".cooldown", this.cooldownSeconds);
        }
        return cooldownSeconds;
    }

    public int getReducedCooldownSeconds() {
        Effectsmp instance = Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getEffectsConfig() != null) {
            int defaultReduced = (int) (getCooldownSeconds() * 0.75);
            return instance.getConfigManager().getEffectsConfig().getInt(getConfigKey() + ".reduced_cooldown", defaultReduced);
        }
        return (int) (cooldownSeconds * 0.75);
    }

    public int getPassiveAmplifier() {
        Effectsmp instance = Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getEffectsConfig() != null) {
            return instance.getConfigManager().getEffectsConfig().getInt(getConfigKey() + ".passive_amplifier", this.passiveAmplifier);
        }
        return passiveAmplifier;
    }

    public long getAbilityDurationMillis() {
        int defaultDurationSec = switch (this) {
            case INVISIBILITY -> 10;
            case HERO_OF_VILLAGE -> 120;
            case HASTE -> 30;
            case FIRE_RESISTANCE -> 15;
            case SPEED -> 0;
            case DOLPHIN_GRACE -> 15;
            case HEALTH_BOOST -> 30;
            case WIND_CHARGED -> 15;
            case RESISTANCE -> 20;
            case STRENGTH -> 15;
            case REGENERATION -> 45;
        };
        Effectsmp instance = Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getEffectsConfig() != null) {
            int sec = instance.getConfigManager().getEffectsConfig().getInt(getConfigKey() + ".duration", defaultDurationSec);
            return sec * 1000L;
        }
        return defaultDurationSec * 1000L;
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
            if (!type.isEnabled()) continue;
            if (type.getRarity() == Rarity.COMMON || type.getRarity() == Rarity.RARE) {
                list.add(type);
            }
        }
        return list.toArray(new EffectType[0]);
    }

    public static EffectType[] getOPEffects() {
        java.util.List<EffectType> list = new java.util.ArrayList<>();
        for (EffectType type : values()) {
            if (!type.isEnabled()) continue;
            if (type.getRarity() == Rarity.EPIC || type.getRarity() == Rarity.LEGENDARY) {
                list.add(type);
            }
        }
        return list.toArray(new EffectType[0]);
    }
}
