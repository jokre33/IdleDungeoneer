package eu.jokre.games.idleDungeoneer.entity;

import com.sun.xml.internal.bind.v2.model.core.ID;
import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.Inventory.Item;
import eu.jokre.games.idleDungeoneer.Settings;
import eu.jokre.games.idleDungeoneer.ability.Ability;
import eu.jokre.games.idleDungeoneer.ability.IncomingHealingAbility;
import eu.jokre.games.idleDungeoneer.ability.StatusEffect;
import eu.jokre.games.idleDungeoneer.ability.StatusEffectBuff;
import org.joml.Vector2d;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Vector;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.*;
import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityHitCategories.*;
import static eu.jokre.games.idleDungeoneer.ability.Ability.statusEffectTargets.*;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.characterStates.*;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.resourceTypes.*;

/**
 * Created by jokre on 19-May-17.
 */

public abstract class EntityCharacter extends Entity {
    protected String name;

    public static final int abilityCap = 21;

    protected Ability[] abilities = new Ability[abilityCap];    //Any Entity can have and use up to 19 Active Abilities.
    protected Vector<Aggro> aggroTable = new Vector<>();   // > Slot 0 and 1 are reserved for Auto Hits.
    protected float moveToRange;    //Maximum Range to target this Entity tries to keep.
    protected float aggroModifier = 1;

    public static final float meleeRange = 0;
    public static final float castRange = 6;

    public double getHaste() {
        return haste;
    }

    //Character States
    public enum characterStates {
        WAITING,    //Character is doing nothing. (Waiting for GCD, a new Enemy to hit, resources or Cooldowns)
        MOVING,     //Character is moving. Blocks all Abilities with a Cast time.
        CASTING,    //Character is casting a Spell. Off GCD Abilities can't be used and any attempt to move will be blocked.
        STUNNED     //Character is Stunned. No Actions are possible.
    }

    protected characterStates characterStatus = WAITING;    //Defaults to waiting TODO: actually implement that LUL
    protected Instant characterStatusUntil;                 //When is the Character available again
    protected Ability castingAbility;                       //Used in conjunction with the CASTING status. When casting is done this ability will be used.
    protected EntityCharacter castingTarget;

    boolean isEnemy;
    boolean tank;
    protected Vector<IncomingHealingAbility> incomingHealingSpells = new Vector<>();

    /*********************
     * Stats and Scaling *
     *********************/

    public static final int maxLevel = 50;
    int level;        //Increases Power of Player Characters and Enemy Characters alike.
    float itemlevel;  //Only directly increases the Power of Enemy Characters. It acts as a gauging point for Player Character Power.

    public static final double itemLevelScaling = 1.15;
    public static final int itemLevelScalingPerXAmount = 15;

    public static final float[] ratingConversion = {0,
            51, 52, 53, 54, 55, 56, 57, 58, 59, 60,   //Level  1 - 10
            61, 62, 63, 64, 65, 66, 67, 68, 69, 70,   //Level 11 - 20
            71, 72, 73, 74, 75, 76, 77, 78, 79, 80,   //Level 21 - 30
            81, 82, 83, 84, 85, 86, 87, 88, 89, 90,   //Level 31 - 40
            91, 92, 93, 94, 95, 96, 97, 98, 99, 100,  //Level 41 - 50
            101, 102, 103                             //For Enemies
    };

    public static final int statGainPerLevel = 5;
    public static final int primaryStatBonus = 3;
    public static final int staminaGainPerLevel = 10;

    public static final int statBonusLevels = 10;

    //Strength
    protected double strength = 0;
    public static final double parryFromStrength = 0.5;
    public static final double blockFromStrength = 0.5;
    public static final double attackPowerFromStrength = 1;

    //Agility
    protected double agility = 0;
    public static final double dodgeFromAgility = 0.5;
    public static final double critFromAgility = 0.5;
    public static final double attackPowerFromAgility = 1;

    //Intelligence
    protected double intelligence = 0;
    public static final double critFromIntelligence = 0.5;
    public static final double manaFromIntelligence = 5;
    public static final double spellPowerFromIntelligence = 1;

    //Stamina & Health
    protected double stamina = 0;
    public static final double healthFromStamina = 5;
    public static final double tankStaminaBonus = 0.5;
    protected double health;
    protected double maximumHealth;
    protected double healthRegeneration;

    //Resources
    public enum resourceTypes {
        RAGE,
        MANA,
        ENERGY
    }

    protected resourceTypes resourceType = MANA;
    protected double resource;
    protected double maximumResource;
    protected double resourceRegeneration;

    //Accuracy
    protected double accuracyRating;
    protected double accuracy = 0;

    //Critical Chance / Damage
    protected double criticalStrikeRating;
    protected double criticalStrikeChance;
    public static final double criticalStrikeChanceBase = 0.05;
    public static final double criticalStrikeChanceSuppressionPerLevel = 0.01; //1% per Level below the Target is calculated after Overflow

    //Haste & CDR
    protected double hasteRating;
    protected double haste;
    protected double cooldownReduction;

    //Parry
    protected double parryRating;
    protected double parryChance;
    public static final double parryBase = 0.05;
    public static final double parryPerLevelDifference = 0.01; //+1% per level above the Attacker -1% per Level below
    public static final double parryRatingConversionMultiplier = 250; //To offset the Quadratic growth a bit
    //TODO: Implement Parry & Dodge diminishing returns

    //Dodge
    protected double dodgeRating;
    protected double dodgeChance;
    public static final double dodgeBase = 0.05;
    public static final double dodgePerLevelDifference = 0.01; //+1% per level above the Attacker -1% per Level below
    public static final double dodgeRatingConversionMultiplier = 250; //To offset the Quadratic growth a bit

    //Block
    protected double blockRating;
    protected double blockMultiplier;
    protected double blockChance;
    protected double blockAmount;
    protected boolean canBlock; //True if the Entity can Block
    public static final double blockRatingConversionMultiplier = 100;

    //Armor & Resistance
    protected double armorRating;
    protected double armorDamageReduction;
    protected double resistance;
    protected Item.armorClass armorClass;

    //Attack Power & Weapon
    protected double attackPower;
    protected double weaponDamageMin;
    protected double weaponDamageMax;
    protected Duration weaponAttackSpeed;
    protected boolean dualWielding;     //Is the Character using a second Weapon in the Offhand. This adds a Second Auto Attack ability.
    protected double weapon2DamageMin;
    protected double weapon2DamageMax;
    protected Duration weapon2AttackSpeed;
    protected double missChance;
    public static final double baseMissChance = 0.05;
    public static final double dualWieldMissChanceIncrease = 0.1;
    public static final double attackPowerConversionToWeaponDamage = 0.5;

    //Spell Power
    protected double spellPower;

    /****************/

    protected Item[] items = new Item[16];

    protected Vector<StatusEffect> currentBuffs = new Vector<>();
    protected Vector<StatusEffect> currentDebuffs = new Vector<>();

    protected EntityCharacter target;

    boolean dead = false;

    protected Instant globalCooldownUntil = Instant.now();
    protected Ability lastAbility = null;

    Instant deathTime = null;
    Instant lastTick = Instant.now();

    EntityCharacter(int level, float itemlevel, Vector2d position, String name, boolean isEnemy, float hitboxRadius) {
        super(position, hitboxRadius);
        this.level = level;
        this.itemlevel = itemlevel;
        this.name = name;
        this.isEnemy = isEnemy;
        this.tank = false;
    }

    public void generateResource(double amount) {
        this.resource += amount;
        if (this.resource > this.maximumResource) this.resource = this.maximumResource;
    }


    public boolean isTank() {
        return tank;
    }

    abstract void updateStats();

    void triggerGlobalCooldown() {
        double GCD = Settings.globalCooldown.toMillis();
        long hastedGCD = Math.round(GCD / (1 + haste));
        this.globalCooldownUntil = Instant.now().plus(Duration.ofMillis(hastedGCD));
    }

    public float globalCooldownRemaining() {
        return (float) Duration.between(Instant.now(), this.globalCooldownUntil).toMillis() / 1000f;
    }

    private void processAggro() {
        if (!this.aggroTable.isEmpty()) {
            Collections.sort(this.aggroTable);
            double currentTargetAggro = 0;
            for (Aggro a : this.aggroTable) {
                if (this.target == a.getTarget()) {
                    currentTargetAggro = a.getAmount();
                }
            }
            if (this.aggroTable.firstElement().getAmount() > (1.1 * currentTargetAggro)) {
                this.target = this.aggroTable.firstElement().getTarget();
            }
        }
    }

    public void generateAggro(EntityCharacter target, double amount) {
        boolean characterOnTable = false;
        amount *= target.getAggroModifier();
        if (!this.aggroTable.isEmpty()) {
            for (int i = 0; i < this.aggroTable.size(); i++) {
                if (this.aggroTable.elementAt(i).getTarget() == target) {
                    characterOnTable = true;
                    this.aggroTable.elementAt(i).add(amount);
                    break;
                }
            }
        }
        if (!characterOnTable) {
            this.aggroTable.addElement(new Aggro(amount, target));
        }
        processAggro();
    }

    public void getHealed(EntityCharacter caster, double amount) {
        this.health += amount;
        if (this.health > this.maximumHealth) this.health = this.maximumHealth;
    }

    public double getParryChance() {
        return parryChance;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public double getBlockChance() {
        return blockChance;
    }

    public Ability.abilityHitCategories performCombatRoll(EntityCharacter t, Ability a) {
        Ability.abilityHitCategories hitType;
        double roll = Math.random();
        if (a.getAbilityCategory() == SPELL) {
            double critChance = this.criticalStrikeChance;
            if (t.getLevel() > this.getLevel()) {
                critChance -= criticalStrikeChanceSuppressionPerLevel * (t.getLevel() - this.getLevel());
            }
            if (roll < critChance) {
                hitType = ABILITY_CRIT;
            } else {
                hitType = ABILITY_HIT;
            }
        } else {
            double accuracy = this.accuracy;
            double missChance = this.missChance;
            double dodgeChance = t.getDodgeChance();
            double parryChance = t.getParryChance();
            double blockChance = 0;
            if (t.canBlock()) {
                blockChance = t.getBlockChance();
            }
            double critChance = this.criticalStrikeChance;
            if (t.getLevel() > this.getLevel()) {
                critChance -= criticalStrikeChanceSuppressionPerLevel * (target.getLevel() - this.getLevel());
            }
            dodgeChance += (t.getLevel() - this.getLevel()) * dodgePerLevelDifference;
            parryChance += (t.getLevel() - this.getLevel()) * parryPerLevelDifference;

            if (accuracy > 0) {
                missChance -= accuracy;
                accuracy -= this.missChance;
                if (missChance < 0) missChance = 0;
            }
            if (accuracy > 0) {
                dodgeChance -= accuracy;
                accuracy -= t.getDodgeChance() + (t.getLevel() - this.getLevel()) * dodgePerLevelDifference;
                if (dodgeChance < 0) dodgeChance = 0;
            }
            if (accuracy > 0) {
                parryChance -= accuracy;
                if (parryChance < 0) parryChance = 0;
            }

            if (roll < missChance) {
                hitType = ABILITY_MISS;
            } else if (roll < (missChance + dodgeChance)) {
                hitType = ABILITY_DODGE;
            } else if (roll < (missChance + dodgeChance + parryChance)) {
                hitType = ABILITY_PARRY;
            } else if (roll < (missChance + dodgeChance + parryChance + blockChance)) {
                hitType = ABILITY_BLOCK;
            } else if (roll < (missChance + dodgeChance + parryChance + blockChance + critChance)) {
                hitType = ABILITY_CRIT;
            } else {
                hitType = ABILITY_HIT;
            }
        }
        return hitType;
    }

    public void takeDamage(EntityCharacter t, double a) {
        if (!this.isDead()) {
            this.health -= a;
            this.dead = this.health <= 0;
            if (this.dead) this.deathTime = Instant.now();
            if (this.health <= 0) this.health = 0;
            this.generateAggro(t, a * t.getAggroModifier());
        }
    }

    public void attack(EntityCharacter target, Ability ability) {
        attack(target, ability, false);
    }

    public void attack(EntityCharacter target, Ability ability, boolean areaDamageOverride) {
        Ability.abilityHitCategories hitType = this.performCombatRoll(target, ability);
        double abilityDamage = 0;

        switch (ability.getAbilityCategory()) {
            case MELEE:
                abilityDamage = this.attackPower * ability.getScaleFactor() * (0.95 + Math.random() * 0.1);
                abilityDamage *= 1 - target.armorDamageReduction;
                break;
            case WEAPON:
                abilityDamage = Math.random() * (this.weaponDamageMax - this.weaponDamageMin) + this.weaponDamageMin; //TODO: add Attack Power to Weapon Damage calculation.
                abilityDamage *= ability.getScaleFactor();
                abilityDamage *= 1 - target.armorDamageReduction;
                break;
            case SPELL:
                abilityDamage = this.spellPower * ability.getScaleFactor() * (0.95 + Math.random() * 0.1);
                abilityDamage -= target.resistance;
        }
        if (areaDamageOverride) abilityDamage *= ability.getAreaDamageModifier();
        switch (hitType) {
            case ABILITY_HIT:
                ability.onHit(target);
                target.takeDamage(this, abilityDamage);
                break;
            case ABILITY_CRIT:
                abilityDamage *= 2;
                ability.onCrit(target);
                target.takeDamage(this, abilityDamage);
                break;
            case ABILITY_MISS:
                abilityDamage *= 0;
                ability.onMiss(target);
                break;
            case ABILITY_BLOCK:
                abilityDamage -= target.blockAmount;
                if (abilityDamage < 0) abilityDamage = 0;
                ability.onBlock(target);
                target.takeDamage(this, abilityDamage);
                break;
            case ABILITY_DODGE:
                abilityDamage *= 0;
                ability.onDodge(target);
                break;
            case ABILITY_PARRY:
                abilityDamage *= 0;
                ability.onParry(target);
                break;
        }
        if (abilityDamage > 0) {
            System.out.println(Instant.now().toString() + " " + this.getName() + " " + ability.getName() + " " + hitType.toString() + " " + target.getName() + " " + abilityDamage);
        } else {
            System.out.println(Instant.now().toString() + " " + this.getName() + " " + ability.getName() + " " + hitType.toString() + " " + target.getName());
        }

        //TODO: Split Area of Effect damage on Main Target to allow lower damage on secondary Targets
        if (ability.hasAreaOfEffect() && !areaDamageOverride) {
            if (!this.isEnemy) {
                if (ability.getAreaOfEffectLocation() == Ability.areaOfEffectLocations.TARGET) {
                    for (EnemyCharacter enemyCharacter : IdleDungeoneer.idleDungeoneer.getEnemyCharacters()) {
                        if (target.getHitboxDistance(enemyCharacter) <= ability.getAreaOfEffectRange() && enemyCharacter != target)
                            this.attack(enemyCharacter, ability, true);
                    }
                } else {
                    for (EnemyCharacter enemyCharacter : IdleDungeoneer.idleDungeoneer.getEnemyCharacters()) {
                        if (this.getHitboxDistance(enemyCharacter) <= ability.getAreaOfEffectRange() && enemyCharacter != target)
                            this.attack(enemyCharacter, ability, true);
                    }
                }
            } else {
                if (ability.getAreaOfEffectLocation() == Ability.areaOfEffectLocations.TARGET) {
                    for (PlayerCharacter playerCharacter : IdleDungeoneer.idleDungeoneer.getPlayerCharacters()) {
                        if (target.getHitboxDistance(playerCharacter) <= ability.getAreaOfEffectRange() && playerCharacter != target)
                            this.attack(playerCharacter, ability, true);
                    }
                } else {
                    for (PlayerCharacter playerCharacter : IdleDungeoneer.idleDungeoneer.getPlayerCharacters()) {
                        if (this.getHitboxDistance(playerCharacter) <= ability.getAreaOfEffectRange() && playerCharacter != target)
                            this.attack(playerCharacter, ability, true);
                    }
                }
            }
        }
    }

    public boolean useAbility(EntityCharacter t, Ability a) {
        if ((this.globalCooldownRemaining() <= 0 || !a.isOnGlobalCooldown()) && this.resource >= a.getCost()
                && (this.characterStatus == WAITING || this.characterStatus == MOVING) && this.inAbilityRange(a) && a.cooldownReady()) {
            if (a.hasCastTime()) {
                if (this.characterStatus == WAITING) {
                    startCasting(t, a);
                    if (a.isOnGlobalCooldown()) triggerGlobalCooldown();
                    return true;
                }
            } else if (a.hasProjectile()) {
                a.onCast(t);
                a.createProjectile(t);
                if (a.isOnGlobalCooldown()) triggerGlobalCooldown();
                return true;
            } else {
                a.onCast(t);
                attack(t, a);
                if (a.isOnGlobalCooldown()) triggerGlobalCooldown();
                return true;
            }
        }
        return false;
    }

    public void startCasting(EntityCharacter t, Ability a) {
        this.characterStatus = CASTING;
        this.castingTarget = t;
        this.castingAbility = a;
        double castTime = a.getCastTime().toMillis();
        long hastedCastTime = Math.round(castTime / (1 + haste));
        this.characterStatusUntil = Instant.now().plus(Duration.ofMillis(hastedCastTime));
        this.triggerGlobalCooldown();
    }

    public void disableAttack() {
        this.abilities[0].disable();
        this.abilities[1].disable();
    }

    public float getAggroModifier() {
        return aggroModifier;
    }

    public void setAggroModifier(float aggroModifier) {
        this.aggroModifier = aggroModifier;
    }

    public Vector<StatusEffect> getCurrentBuffs() {
        return currentBuffs;
    }

    public Vector<StatusEffect> getCurrentDebuffs() {
        return currentDebuffs;
    }

    public boolean hasBuff(Class statusEffect) {
        for (StatusEffect e : this.currentBuffs) {
            if (e.getClass() == statusEffect) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBuff(StatusEffect statusEffect) {
        return this.currentBuffs.contains(statusEffect);
    }

    public boolean hasDebuff(StatusEffect statusEffect) {
        return this.currentDebuffs.contains(statusEffect);
    }

    public boolean hasBuff(Class statusEffectType, EntityCharacter caster) {
        for (StatusEffect e : this.currentBuffs) {
            if (e.getClass() == statusEffectType && e.getCaster() == caster) {
                return true;
            }
        }
        return false;
    }

    public void applyBuff(StatusEffect statusEffect) {
        if (this.currentBuffs.contains(statusEffect)) {
            this.currentBuffs.elementAt(this.currentBuffs.indexOf(statusEffect)).refresh();
        } else {
            this.currentBuffs.addElement(statusEffect);
        }
    }

    public void removeBuff(StatusEffect statusEffect) {
        if (this.currentBuffs.contains(statusEffect)) this.currentBuffs.removeElement(statusEffect);
    }

    public void removeBuff(Class statusEffect) {
        Vector<StatusEffect> markForRemoval = new Vector<>();
        for (StatusEffect e : this.currentBuffs) {
            if (e.getClass() == statusEffect) {
                markForRemoval.addElement(e);
            }
        }
        this.currentBuffs.removeAll(markForRemoval);
    }

    public void removeBuff(EntityCharacter caster) {
        Vector<StatusEffect> markForRemoval = new Vector<>();
        for (StatusEffect e : this.currentBuffs) {
            if (e.getCaster() == caster) {
                markForRemoval.addElement(e);
            }
        }
        this.currentBuffs.removeAll(markForRemoval);
    }

    public void applyDebuff(StatusEffect statusEffect) {
        if (this.currentDebuffs.contains(statusEffect)) {
            this.currentDebuffs.elementAt(this.currentDebuffs.indexOf(statusEffect)).refresh();
        } else {
            this.currentDebuffs.addElement(statusEffect);
        }
    }

    public void removeDebuff(StatusEffect statusEffect) {
        if (this.currentDebuffs.contains(statusEffect)) this.currentDebuffs.removeElement(statusEffect);
    }

    protected void findTarget() {
        if (!aggroTable.isEmpty()) {
            for (Aggro aggro : aggroTable) {
                if (!aggro.getTarget().isDead()) {
                    this.target = aggro.getTarget();
                    break;
                }
            }
        }
    }

    public boolean inAbilityRange(Ability ability) {
        if (this.hasTarget()) {
            return (getHitboxDistance(this.getTarget()) <= ability.getRange());
        }
        return false;
    }

    public boolean moveToEntity(Entity e, long timeMoving, float range) {
        if (getHitboxDistance(e) > range) {
            double xDistance = e.getPosition().x - this.getPosition().x;
            double yDistance = e.getPosition().y - this.getPosition().y;
            double angle = Math.asin(Math.abs(xDistance) / getDistance(e));
            double moveX = Math.sin(angle) * this.movementSpeed * timeMoving / 1000;
            double moveY = Math.cos(angle) * this.movementSpeed * timeMoving / 1000;
            if (xDistance < 0) moveX *= -1;
            if (yDistance < 0) moveY *= -1;
            this.position.x += moveX;
            this.position.y += moveY;
        }

        if (getHitboxDistance(e) <= range) {
            return true;
        } else {
            return false;
        }
    }

    private void moveAwayFromNearestEntity(long timeMoving) {
        Entity e = this.getNearestEntity();
        if (e != null && getHitboxDistance(e) < (-0.1)) {
            double xDistance = e.getPosition().x - this.getPosition().x;
            double yDistance = e.getPosition().y - this.getPosition().y;
            double angle = Math.asin(Math.abs(xDistance) / getDistance(e));
            double moveX = Math.sin(angle) * this.movementSpeed * timeMoving / 1000;
            double moveY = Math.cos(angle) * this.movementSpeed * timeMoving / 1000;
            if (xDistance < 0) moveX *= -1;
            if (yDistance < 0) moveY *= -1;
            this.position.x -= moveX;
            this.position.y -= moveY;
        }
    }

    public boolean moveToTarget(long timeMoving) {
        return moveToEntity(this.getTarget(), timeMoving, this.moveToRange);
    }

    public void useResource(double resource) {
        this.resource -= resource;
    }

    public characterStates getCharacterStatus() {
        return characterStatus;
    }

    public Instant getCharacterStatusUntil() {
        return characterStatusUntil;
    }

    public double getSpellPower() {
        return spellPower;
    }

    public double getAttackPower() {
        return attackPower;
    }

    public double getWeaponDamageMin() {
        return weaponDamageMin;
    }

    public double getWeaponDamageMax() {
        return weaponDamageMax;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public EntityCharacter getTarget() {
        return target;
    }

    public void setTarget(EntityCharacter target) {
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean canBlock() {
        return canBlock;
    }

    public double getCriticalStrikeChance() {
        return criticalStrikeChance;
    }

    public double getHealth() {
        return health;
    }

    public double getMaximumHealth() {
        return maximumHealth;
    }

    public double getResource() {
        return resource;
    }

    public double getMaximumResource() {
        return maximumResource;
    }

    public resourceTypes getResourceType() {
        return resourceType;
    }

    public int getLevel() {
        return level;
    }

    public float getItemlevel() {
        return itemlevel;
    }

    public Ability[] getAbilities() {
        return abilities;
    }

    public void addAbility(Ability ability, int slot) {
        this.abilities[slot] = ability;
    }

    public float getSize() {
        return this.hitboxRadius * 2;
    }

    public int removeAbility(Ability ability) {
        int abilitySlot = abilitySlot(ability);
        if (abilitySlot >= 0) removeAbility(abilitySlot);
        return abilitySlot;
    }

    public void removeAbility(int slot) {
        this.abilities[slot] = null;
    }

    public void replaceAbility(Ability ability, int slot) {
        removeAbility(slot);
        addAbility(ability, slot);
    }

    public void replaceAbility(Ability newAbility, Ability oldAbility) {
        int abilitySlot = removeAbility(oldAbility);
        if (abilitySlot >= 0) addAbility(newAbility, abilitySlot);
    }

    public Ability abilityInSlot(int slot) {
        return abilities[slot];
    }

    public int abilitySlot(Ability ability) {
        for (int i = 0; i < abilities.length; i++) {
            if (abilities[i] == ability) {
                return i;
            }
        }
        return -1;
    }

    protected void timedActions(long timeSinceLastTick) {
        if ((this.getCharacterStatus() == WAITING || this.getCharacterStatus() == MOVING) && this.target != null) {
            if (!moveToTarget(timeSinceLastTick)) {
                this.characterStatus = MOVING;
                this.characterStatusUntil = Instant.now().plus(Duration.ofMillis(1000));
            } else {
                this.characterStatus = WAITING;
                this.characterStatusUntil = Instant.now().minus(Duration.ofMillis(1000));
            }
        }
        if (this.resource < this.maximumResource) {
            this.resource += timeSinceLastTick * resourceRegeneration / 1000;
        }
        if (this.resource > this.maximumResource) {
            this.resource = this.maximumResource;
        }
        if (this.health < this.maximumHealth) {
            this.health += timeSinceLastTick * healthRegeneration / 1000;
        }
        if (this.health > this.maximumHealth) {
            this.health = this.maximumHealth;
        }
        if (incomingHealingSpells.size() > 0) {
            Vector<IncomingHealingAbility> mfr = new Vector<>();
            for (IncomingHealingAbility iha : incomingHealingSpells) {
                if (Instant.now().isAfter(iha.getTimeout())) mfr.addElement(iha);
            }
            incomingHealingSpells.removeAll(mfr);
        }
        if (!currentBuffs.isEmpty()) {
            Vector<StatusEffect> mfr = new Vector<>();
            for (StatusEffect buff : currentBuffs) {
                if (!buff.tick()) mfr.addElement(buff);
            }
            currentBuffs.removeAll(mfr);
        }
        if (!currentDebuffs.isEmpty()) {
            Vector<StatusEffect> mfr = new Vector<>();
            for (StatusEffect debuff : currentDebuffs) {
                if (!debuff.tick()) mfr.addElement(debuff);
            }
            currentDebuffs.removeAll(mfr);
        }
        //if (!this.isEnemy && this.characterStatus == WAITING) this.moveAwayFromNearestEntity(timeSinceLastTick);
    }

    public boolean tick() {
        Instant now = Instant.now();
        long timeSinceLastTick = ChronoUnit.MILLIS.between(this.lastTick, now);
        this.lastTick = now;
        this.timedActions(timeSinceLastTick);
        this.ai();
        return !this.isDead();
    }

    public void removeAggroTarget(EntityCharacter e) {
        if (!this.aggroTable.isEmpty()) {
            for (int i = 0; i < this.aggroTable.size(); i++) {
                if (this.aggroTable.elementAt(i).getTarget() == e) {
                    this.aggroTable.remove(i);
                    if (this.target == e) {
                        this.target = null;
                    }
                    break;
                }
            }
        }
        this.processAggro();
    }

    public double getIncomingHealing() {
        double incomingHealing = 0;
        for (IncomingHealingAbility iha : incomingHealingSpells) {
            incomingHealing += iha.getAbility().getScaleFactor() * iha.getAbility().getOwner().getSpellPower();
        }
        return incomingHealing;
    }

    public void addIncomingHealingSpell(Ability a) {
        this.incomingHealingSpells.addElement(new IncomingHealingAbility(a));
    }

    public void removeIncomingHealingSpell(EntityCharacter c) {
        Vector<IncomingHealingAbility> mfr = new Vector<>();
        for (IncomingHealingAbility iha : incomingHealingSpells) {
            if (iha.getAbility().getOwner() == c) {
                mfr.addElement(iha);
            }
        }
        incomingHealingSpells.removeAll(mfr);
    }

    public double getExpectedHealth() {
        return this.getIncomingHealing() + this.getHealth();
    }

    public void ai() {
        //Character Status
        if (this.characterStatus != WAITING) {
            if (this.characterStatus == CASTING) {
                if (this.castingTarget.isDead() || this.castingTarget == null) {
                    this.characterStatus = WAITING;
                    this.castingTarget = null;
                }
                if (Instant.now().isAfter(this.characterStatusUntil)) {
                    this.castingAbility.onCast(this.castingTarget);
                    if (this.castingAbility.hasProjectile()) {
                        this.castingAbility.createProjectile(this.castingTarget);
                    } else {
                        this.attack(this.castingTarget, this.castingAbility);
                    }
                    this.characterStatus = WAITING;
                }
            } else if (Instant.now().isAfter(this.characterStatusUntil)) {
                this.characterStatus = WAITING;
            }
        }
        this.abilityPriorityList();
        this.meleeHit();
        //this.abilityHandling();
    }

    private Entity getNearestEntity() {
        double nearestDistance = 0;
        double distance;
        Entity nearestEntity = null;
        for (Entity e : IdleDungeoneer.idleDungeoneer.getPlayerCharacters()) {
            if (e != this) {
                distance = this.getHitboxDistance(e);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestEntity = e;
                }
            }
        }
        for (Entity e : IdleDungeoneer.idleDungeoneer.getEnemyCharacters()) {
            if (e != this) {
                distance = this.getHitboxDistance(e);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestEntity = e;
                }
            }

        }
        return nearestEntity;
    }

    public Vector<EntityCharacter> getAlliesInRange(double range) {
        Vector<EntityCharacter> alliesInRange = new Vector<>();
        if (this.isEnemy()) {
            for (EnemyCharacter e : IdleDungeoneer.idleDungeoneer.getEnemyCharacters()) {
                if (this.getHitboxDistance(e) <= range) alliesInRange.addElement(e);
            }
        } else {
            for (PlayerCharacter p : IdleDungeoneer.idleDungeoneer.getPlayerCharacters()) {
                if (this.getHitboxDistance(p) <= range) alliesInRange.addElement(p);
            }
        }
        return alliesInRange;
    }

    protected void meleeHit() {
        if (this.abilities[0].isEnabled()) useAbility(this.target, this.abilities[0]);
        if (this.abilities[1].isEnabled()) useAbility(this.target, this.abilities[1]);
    }

    protected abstract void abilityPriorityList();

    protected boolean hasTarget() {
        return target != null;
    }

    public String getResourceName() {
        switch (resourceType) {
            case ENERGY:
                return "Energy";
            case RAGE:
                return "Rage";
            case MANA:
                return "Mana";
            default:
                return "Mana";
        }
    }

    public Ability currentlyCastingAbility() {
        return castingAbility;
    }

    public Ability getLastAbility() {
        return lastAbility;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public Item.armorClass getArmorClass() {
        return this.armorClass;
    }

    private class Aggro implements Comparable<Aggro> {
        private double amount;
        private EntityCharacter target;

        public Aggro(double amount, EntityCharacter target) {
            this.amount = amount;
            this.target = target;
        }

        public double getAmount() {
            return amount;
        }

        public EntityCharacter getTarget() {
            return target;
        }

        public void add(double amount) {
            this.amount += amount;
        }

        public void reduce(double percentage) {
            this.amount *= 1 - percentage;
        }

        public void drop() {
            this.reduce(1);
        }

        @Override
        public int compareTo(Aggro o) {
            int cmpInt = 0;
            if (o.getAmount() < this.getAmount()) {
                cmpInt = -1;
            } else if (o.getAmount() > this.getAmount()) {
                cmpInt = 1;
            }
            return cmpInt;
        }
    }
}
