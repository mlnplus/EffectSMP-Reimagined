package mlnplus.hu.effectsmp.data;

import mlnplus.hu.effectsmp.effects.EffectType;

import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private String playerName;

    private EffectType effect;
    private boolean passiveEnabled;
    private int effectHearts;
    private boolean hasEffectShard;

    private int kills;
    private int deaths;
    private boolean firstDeathOccurred;

    private Set<UUID> trustedPlayers;

    private long lastAbilityCooldown;
    private long abilityActiveUntil;
    private long haste3x3ActiveUntil;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.effect = null;
        this.passiveEnabled = true;
        this.effectHearts = 1;
        this.hasEffectShard = true;
        this.kills = 0;
        this.deaths = 0;
        this.firstDeathOccurred = false;
        this.trustedPlayers = new HashSet<>();
        this.lastAbilityCooldown = 0;
        this.abilityActiveUntil = 0;
        this.haste3x3ActiveUntil = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public EffectType getEffect() {
        return effect;
    }

    public void setEffect(EffectType effect) {
        this.effect = effect;
    }

    public boolean isPassiveEnabled() {
        return passiveEnabled;
    }

    public void setPassiveEnabled(boolean passiveEnabled) {
        this.passiveEnabled = passiveEnabled;
    }

    public int getEffectHearts() {
        return effectHearts;
    }

    public void setEffectHearts(int effectHearts) {
        int maxHearts = 10;
        mlnplus.hu.effectsmp.Effectsmp instance = mlnplus.hu.effectsmp.Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getConfig() != null) {
            maxHearts = instance.getConfigManager().getConfig().getInt("settings.max-hearts", 10);
        }
        int value = Math.max(0, effectHearts);
        if (maxHearts > 0) {
            value = Math.min(maxHearts, value);
        }
        this.effectHearts = value;
    }

    public void addEffectHearts(int amount) {
        setEffectHearts(this.effectHearts + amount);
    }

    public void removeEffectHearts(int amount) {
        setEffectHearts(this.effectHearts - amount);
    }

    public boolean hasEffectShard() {
        return hasEffectShard;
    }

    public void setHasEffectShard(boolean hasEffectShard) {
        this.hasEffectShard = hasEffectShard;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public boolean isFirstDeathOccurred() {
        return firstDeathOccurred;
    }

    public void setFirstDeathOccurred(boolean firstDeathOccurred) {
        this.firstDeathOccurred = firstDeathOccurred;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void addTrustedPlayer(UUID uuid) {
        trustedPlayers.add(uuid);
    }

    public void removeTrustedPlayer(UUID uuid) {
        trustedPlayers.remove(uuid);
    }

    public boolean hasTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid);
    }

    public long getLastAbilityCooldown() {
        return lastAbilityCooldown;
    }

    public void setLastAbilityCooldown(long lastAbilityCooldown) {
        this.lastAbilityCooldown = lastAbilityCooldown;
    }

    public long getAbilityActiveUntil() {
        return abilityActiveUntil;
    }

    public void setAbilityActiveUntil(long abilityActiveUntil) {
        this.abilityActiveUntil = abilityActiveUntil;
    }

    public boolean isAbilityActive() {
        return System.currentTimeMillis() < abilityActiveUntil;
    }

    public long getEffectiveCooldownMillis() {
        if (effect == null)
            return 0;
        int req = 3;
        mlnplus.hu.effectsmp.Effectsmp instance = mlnplus.hu.effectsmp.Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getConfig() != null) {
            req = instance.getConfigManager().getConfig().getInt("settings.hearts-required.cooldown-reduction", 3);
        }

        if (effectHearts >= req) {
            return effect.getReducedCooldownSeconds() * 1000L;
        } else {
            return effect.getCooldownSeconds() * 1000L;
        }
    }

    public boolean isAbilityOnCooldown() {
        if (effect == null)
            return false;
        long effectiveCooldown = getEffectiveCooldownMillis();
        long cooldownEnd = lastAbilityCooldown + effectiveCooldown;
        return System.currentTimeMillis() < cooldownEnd;
    }

    public long getRemainingCooldown() {
        if (effect == null)
            return 0;
        long effectiveCooldown = getEffectiveCooldownMillis();
        long cooldownEnd = lastAbilityCooldown + effectiveCooldown;
        return Math.max(0, cooldownEnd - System.currentTimeMillis());
    }

    public long getRemainingAbilityDuration() {
        return Math.max(0, abilityActiveUntil - System.currentTimeMillis());
    }

    public boolean canUseAbility() {
        int req = 3;
        mlnplus.hu.effectsmp.Effectsmp instance = mlnplus.hu.effectsmp.Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getConfig() != null) {
            req = instance.getConfigManager().getConfig().getInt("settings.hearts-required.active-ability", 3);
        }
        return effectHearts >= req && !isAbilityOnCooldown() && !isAbilityActive();
    }

    public int getPassiveAmplifier() {
        int req = 2;
        int add = 1;
        mlnplus.hu.effectsmp.Effectsmp instance = mlnplus.hu.effectsmp.Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getConfig() != null) {
            req = instance.getConfigManager().getConfig().getInt("settings.hearts-required.passive-boost", 2);
            add = instance.getConfigManager().getConfig().getInt("settings.modifiers.passive-boost-amplifier-add", 1);
        }
        return effectHearts >= req ? add : 0;
    }

    public double getCooldownMultiplier() {
        int req = 3;
        double mult = 0.75;
        mlnplus.hu.effectsmp.Effectsmp instance = mlnplus.hu.effectsmp.Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getConfig() != null) {
            req = instance.getConfigManager().getConfig().getInt("settings.hearts-required.cooldown-reduction", 3);
            mult = instance.getConfigManager().getConfig().getDouble("settings.modifiers.cooldown-reduction-multiplier", 0.75);
        }
        return effectHearts >= req ? mult : 1.0;
    }

    public int getEffectiveCooldownSeconds() {
        if (effect == null)
            return 0;
        return (int) (getEffectiveCooldownMillis() / 1000L);
    }

    public boolean canAccessMenu() {
        int req = 1;
        mlnplus.hu.effectsmp.Effectsmp instance = mlnplus.hu.effectsmp.Effectsmp.getInstance();
        if (instance != null && instance.getConfigManager() != null && instance.getConfigManager().getConfig() != null) {
            req = instance.getConfigManager().getConfig().getInt("settings.hearts-required.menu-access", 1);
        }
        return effectHearts >= req;
    }

    public void clearAbilityCooldown() {
        this.lastAbilityCooldown = 0;
    }

    public long getHaste3x3ActiveUntil() {
        return haste3x3ActiveUntil;
    }

    public void setHaste3x3ActiveUntil(long haste3x3ActiveUntil) {
        this.haste3x3ActiveUntil = haste3x3ActiveUntil;
    }

    public boolean isHaste3x3Active() {
        return System.currentTimeMillis() < haste3x3ActiveUntil;
    }
}
