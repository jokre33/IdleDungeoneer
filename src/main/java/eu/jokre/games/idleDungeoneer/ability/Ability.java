package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.Settings;
import eu.jokre.games.idleDungeoneer.entity.Entity;
import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;
import eu.jokre.games.idleDungeoneer.entity.Projectile;
import org.joml.Vector2d;

import java.time.Duration;
import java.time.Instant;
import java.util.Vector;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.*;
import static eu.jokre.games.idleDungeoneer.ability.Ability.areaOfEffectLocations.*;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.*;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.*;

/**
 * Created by jokre on 19-May-17.
 */

public abstract class Ability {
    public EntityCharacter getOwner() {
        return owner;
    }

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
        CASTER,
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

    public enum areaOfEffectLocations {
        CASTER,
        TARGET
    }

    protected boolean hasAreaOfEffect;
    protected areaOfEffectLocations areaOfEffectLocation;
    protected float areaOfEffectRange;

    protected EntityCharacter owner = null;

    protected abilityCategories abilityCategory;
    protected damageTypes damageType;
    protected targetCategories targetCategory;

    protected boolean isOnGlobalCooldown;   //Is the Ability affected by the Global Cooldown?
    protected boolean enabled;              //Is the Ability enabled?

    protected boolean hasCastTime;
    protected Duration castTime;

    protected double scaleFactor;           //The Multiplier of the Characters Spell or Attackpower or Weapon Damage this Ability does

    protected Duration cooldown;            //Length of the Cooldown this Ability applies.
    protected Instant availableAfter;       //Timestamp of when the Ability can next be cast.
    protected int cost;                     //Resource cost of the Ability

    protected String name;
    protected float range;

    protected boolean projectile = false;
    protected boolean hastedCooldown = false;
    protected float areaDamageModifier = 1.0f;
    protected float projectileSpeed = 5.0f;
    protected float projectileSize = 0.2f;

    public Ability(EntityCharacter owner) {
        this.owner = owner;
        damageType = PHYSICAL;
        abilityCategory = MELEE;
        targetCategory = ENEMIES;
        scaleFactor = 1;
        cooldown = Settings.globalCooldown;
        cost = 0;
        availableAfter = Instant.now().plusMillis(5000);
        hasCastTime = false;
        castTime = Duration.ZERO;
        range = 1;
        hasAreaOfEffect = false;
        areaOfEffectLocation = TARGET;
    }

    protected void use() {
        if (this.hastedCooldown) {
            double cooldown = this.cooldown.toMillis();
            long hastedCooldown = Math.round(cooldown / (1 + owner.getHaste()));
            this.availableAfter = Instant.now().plus(Duration.ofMillis(hastedCooldown));
        } else {
            this.availableAfter = Instant.now().plus(cooldown);
        }
        owner.useResource(this.getCost());
    }

    public boolean cooldownReady() {
        return Instant.now().isAfter(availableAfter);
    }

    public boolean isCooldownReadyIn(Duration duration) {
        return Instant.now().plus(duration).isAfter(availableAfter);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        this.hasCastTime = castTime != Duration.ZERO;
        this.castTime = castTime;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public boolean hasAreaOfEffect() {
        return hasAreaOfEffect;
    }

    public areaOfEffectLocations getAreaOfEffectLocation() {
        return areaOfEffectLocation;
    }

    public void setAreaOfEffectLocation(areaOfEffectLocations areaOfEffectLocation) {
        this.areaOfEffectLocation = areaOfEffectLocation;
    }

    public float getAreaOfEffectRange() {
        return areaOfEffectRange;
    }

    public void setAreaOfEffectRange(float areaOfEffectRange) {
        this.areaOfEffectRange = areaOfEffectRange;
        this.hasAreaOfEffect = areaOfEffectRange > 0;
    }

    public boolean hasProjectile() {
        return projectile;
    }

    public void setProjectile(boolean projectile) {
        this.projectile = projectile;
    }

    public void createProjectile(EntityCharacter t) {
        IdleDungeoneer.idleDungeoneer.createProjectile(new Projectile(new Vector2d(owner.getPosition().x, owner.getPosition().y), 0.2f, this, t, owner, this.projectileSpeed));
    }

    public float getAreaDamageModifier() {
        return areaDamageModifier;
    }

    public void setAreaDamageModifier(float areaDamageModifier) {
        this.areaDamageModifier = areaDamageModifier;
    }

    public void onCast(EntityCharacter target) {
        this.use();
    }

    public abstract void onHit(EntityCharacter target);

    public abstract void onCrit(EntityCharacter target);

    public abstract void onParry(EntityCharacter target);

    public abstract void onDodge(EntityCharacter target);

    public abstract void onBlock(EntityCharacter target);

    public abstract void onMiss(EntityCharacter target);
}
