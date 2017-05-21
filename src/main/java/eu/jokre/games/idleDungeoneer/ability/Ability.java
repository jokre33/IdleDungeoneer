package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;
import java.time.Instant;
import java.util.Vector;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.*;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.*;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.*;

/**
 * Created by jokre on 19-May-17.
 */

public class Ability {
    public enum damageTypes {
        PHYSICAL,   //Armor gets applied to Damage caused by the Ability
        MAGIC       //Magical resistance gets applied to Damage caused by this Ability
    }

    public enum abilityCategories {
        MELEE,      //Scales over Attack Power and can be Parried, Dodged and Blocked.
        WEAPON,     //Same as Melee but Scales over Weapon Damage instead.
        SPELL       //Scales over Spell Power and can only miss.
    }

    public enum targetCategories {
        FRIENDLIES, //Heal Spells and Buffs on Allies
        ENEMIES     //Damage and Debuffs on Enemies
    }

    public enum statusEffectTargets {
        SELF,
        TARGET
    }

    public enum abilityHitCategories {
        ABILITY_CAST,
        ABILITY_HIT,
        ABILITY_CRIT,
        ABILITY_PARRY,
        ABILITY_DODGE,
        ABILITY_MISS,
        ABILITY_BLOCK
    }

    protected abilityCategories abilityCategory;
    protected damageTypes damageType;
    protected targetCategories targetCategory;
    protected statusEffectTargets statusEffectTarget;
    protected Vector<abilityHitCategories> statusEffectApplicationCondition = new Vector<>();

    protected boolean isOnGlobalCooldown;   //Is the Ability affected by the Global Cooldown?
    protected boolean enabled;              //Is the Ability enabled?

    protected boolean hasCastTime;
    protected Duration castTime;

    protected double scaleFactor;           //The Multiplier of the Characters Spell or Attackpower or Weapon Damage this Ability does
    protected boolean appliesStatusEffect;  //If this Ability leaves a Buff or Debuff on the Target
    protected StatusEffect statusEffect;    //The Buff or Debuff that gets applied

    protected Duration cooldown;            //Length of the Cooldown this Ability applies.
    protected Instant availableAfter;       //Timestamp of when the Ability can next be cast.
    protected int cost;                     //Resource cost of the Ability

    public Ability() {
        damageType = PHYSICAL;
        abilityCategory = MELEE;
        targetCategory = ENEMIES;
        scaleFactor = 1;
        cooldown = IdleDungeoneer.getSettings().getGlobalCooldown();
        cost = 0;
        availableAfter = Instant.now().plusMillis(5000);
        hasCastTime = false;
        castTime = Duration.ZERO;
    }

    public void use(EntityCharacter caster) {
        this.availableAfter = Instant.now().plus(cooldown);
        caster.useResource(this.getCost());
    }

    public boolean cooldownReady() {
        if (Instant.now().isAfter(availableAfter)) {
            return true;
        }
        return false;
    }

    public boolean isCooldownReadyIn(Duration duration) {
        if (Instant.now().plus(duration).isAfter(availableAfter)) {
            return true;
        }
        return false;
    }

    public statusEffectTargets getStatusEffectTarget() {
        return statusEffectTarget;
    }

    public void setStatusEffectTarget(statusEffectTargets statusEffectTarget) {
        this.statusEffectTarget = statusEffectTarget;
    }

    public Vector<abilityHitCategories> getStatusEffectApplicationCondition() {
        return statusEffectApplicationCondition;
    }

    public void addStatusEffectApplicationCondition(abilityHitCategories statusEffectApplicationCondition) {
        this.statusEffectApplicationCondition.addElement(statusEffectApplicationCondition);
    }

    public void removeStatusEffectApplicationCondition(abilityHitCategories statusEffectApplicationCondition) {
        this.statusEffectApplicationCondition.removeElement(statusEffectApplicationCondition);
    }

    public abilityCategories getAbilityCategory() {
        return abilityCategory;
    }

    public damageTypes getDamageType() {
        return damageType;
    }

    public targetCategories getTargetCategory() {
        return targetCategory;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public boolean appliesStatusEffect() {
        return appliesStatusEffect;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    public Duration getCooldown() {
        return cooldown;
    }

    public Instant getAvailableAfter() {
        return availableAfter;
    }

    public int getCost() {
        return cost;
    }

    public void setAbilityCategory(abilityCategories abilityCategory) {
        this.abilityCategory = abilityCategory;
    }

    public void setDamageType(damageTypes damageType) {
        this.damageType = damageType;
    }

    public void setTargetCategory(targetCategories targetCategory) {
        this.targetCategory = targetCategory;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setAppliesStatusEffect(boolean appliesStatusEffect) {
        this.appliesStatusEffect = appliesStatusEffect;
    }

    public void setStatusEffect(StatusEffect statusEffect) {
        this.statusEffect = statusEffect;
    }

    public void setCooldown(Duration cooldown) {
        this.cooldown = cooldown;
    }

    public void setAvailableAfter(Instant availableAfter) {
        this.availableAfter = availableAfter;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public boolean isOnGlobalCooldown() {
        return isOnGlobalCooldown;
    }

    public void setOnGlobalCooldown(boolean onGlobalCooldown) {
        isOnGlobalCooldown = onGlobalCooldown;
    }

    public void triggerGlobalCooldown() {
        Duration gcd = IdleDungeoneer.getSettings().getGlobalCooldown();
        if (this.isOnGlobalCooldown() && isCooldownReadyIn(gcd)) {
            this.availableAfter = Instant.now().plus(gcd);
        }
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasCastTime() {
        return hasCastTime;
    }

    public Duration getCastTime() {
        return castTime;
    }

    public void setCastTime(Duration castTime) {
        if (castTime == Duration.ZERO) {
            this.hasCastTime = false;
        } else {
            this.hasCastTime = true;
        }
        this.castTime = castTime;
    }
}
