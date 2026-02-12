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
        this.effectHearts = Math.max(0, effectHearts);
    }

    public void addEffectHearts(int amount) {
        this.effectHearts += amount;
    }

    public void removeEffectHearts(int amount) {
        this.effectHearts = Math.max(0, this.effectHearts - amount);
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

    public boolean isAbilityOnCooldown() {
        if (effect == null)
            return false;
        long effectiveCooldown = (long) (effect.getCooldownSeconds() * getCooldownMultiplier() * 1000L);
        long cooldownEnd = lastAbilityCooldown + effectiveCooldown;
        return System.currentTimeMillis() < cooldownEnd;
    }

    public long getRemainingCooldown() {
        if (effect == null)
            return 0;
        long effectiveCooldown = (long) (effect.getCooldownSeconds() * getCooldownMultiplier() * 1000L);
        long cooldownEnd = lastAbilityCooldown + effectiveCooldown;
        return Math.max(0, cooldownEnd - System.currentTimeMillis());
    }

    public long getRemainingAbilityDuration() {
        return Math.max(0, abilityActiveUntil - System.currentTimeMillis());
    }

    public boolean canUseAbility() {
        return effectHearts >= 2 && !isAbilityOnCooldown() && !isAbilityActive();
    }

    public int getPassiveAmplifier() {
        return effectHearts >= 2 ? 1 : 0;
    }

    public double getCooldownMultiplier() {
        return effectHearts >= 3 ? 0.75 : 1.0;
    }

    public int getEffectiveCooldownSeconds() {
        if (effect == null)
            return 0;
        return (int) (effect.getCooldownSeconds() * getCooldownMultiplier());
    }

    public boolean canAccessMenu() {
        return effectHearts >= 1;
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
