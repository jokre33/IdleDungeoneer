package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.Inventory.Item;
import eu.jokre.games.idleDungeoneer.ability.Ability;
import eu.jokre.games.idleDungeoneer.ability.StatusEffect;
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
    protected float movementSpeed;  //Movement Speed in Units/Second
    protected float aggroModifier = 1;

    public static final float meleeRange = 1;

    public enum combatRollValues {
        MISS,
        DODGE,
        PARRY,
        CRIT,
        HIT
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

    boolean isEnemy;

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
    public static final double parryFromStrength = 0.75;
    public static final double blockFromStrength = 0.5;
    public static final double attackPowerFromStrength = 1;

    //Agility
    protected double agility = 0;
    public static final double dodgeFromAgility = 0.75;
    public static final double aCritFromAgility = 0.5;
    public static final double attackPowerFromAgility = 1;

    //Intelligence
    protected double intelligence = 0;
    public static final double sCritFromIntelligence = 0.75;
    public static final double manaFromIntelligence = 0.5;
    public static final double spellPowerFromIntelligence = 1;

    //Stamina & Health
    protected double stamina = 0;
    public static final double healthFromStamina = 5;
    protected double health;
    protected double maximumHealth;

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
    private double accuracy = 0;

    //Critical Chance / Damage
    protected double criticalStrikeRating;
    protected double criticalStrikeChance;
    protected double spellCriticalStrikeChance;
    protected double attackCriticalStrikeChance;
    protected double spellCriticalStrikeDamage;
    protected double attackCriticalStrikeDamage;
    public static final double criticalStrikeChanceBase = 0.05;
    public static final double criticalStrikeChanceOverflowIntoCriticalStrikeDamage = 1;
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
    public static final double parryRatingConversionMultiplier = 0.75; //To offset the Quadratic growth a bit
    //TODO: Implement Parry & Dodge diminishing returns

    //Dodge
    protected double dodgeRating;
    protected double dodgeChance;
    public static final double dodgeBase = 0.05;
    public static final double dodgePerLevelDifference = 0.01; //+1% per level above the Attacker -1% per Level below
    public static final double dodgeRatingConversionMultiplier = 0.75; //To offset the Quadratic growth a bit

    //Block
    protected double blockRating;
    protected double blockMultiplier;
    protected double blockChance;
    protected double blockAmount;
    protected boolean canBlock; //True if the Entity can Block
    public static final double blockRatingConversionMultiplier = 1.5;

    //Armor & Resistance
    protected double armorRating;
    protected double armorDamageReduction;
    protected double resistance;

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
    protected boolean targetExists;

    protected Instant globalCooldownUntil = Instant.now();
    protected Ability lastAbility = null;

    Instant deathTime = null;
    Instant lastTick = Instant.now();

    EntityCharacter(int level, float itemlevel, Vector2d position, String name, boolean isEnemy) {
        super(position);
        this.level = level;
        this.itemlevel = itemlevel;
        this.name = name;
        this.isEnemy = isEnemy;
    }

    abstract void updateStats();

    private void triggerGlobalCooldown() {
        this.globalCooldownUntil = Instant.now().plus(IdleDungeoneer.getSettings().getGlobalCooldown());
    }

    public float globalCooldownRemaining() {
        return (float) Duration.between(Instant.now(), this.globalCooldownUntil).toMillis() / 1000f;
    }

    Ability chooseNextCast() {
        for (int i = 2; i < abilityCap; i++) { //Skip Slot 0 and 1 because Auto Hits get handled separately.
            if (this.abilities[i] != null) {
                if (this.abilities[i].cooldownReady() && this.resource >= this.abilities[i].getCost()) {
                    if (characterStatus == WAITING || (characterStatus == MOVING && !this.abilities[i].hasCastTime())) {
                        if (!this.abilities[i].isOnGlobalCooldown() || Instant.now().isAfter(this.globalCooldownUntil)) {
                            return this.abilities[i];
                        }
                    }
                }
                /*
                if (characterStatus == WAITING) {
                    if (this.abilities[i].cooldownReady() && this.resource >= this.abilities[i].getCost()) {
                        return this.abilities[i];
                    }
                } else if (characterStatus == MOVING) {
                    if (this.abilities[i].cooldownReady() && this.resource >= this.abilities[i].getCost() && !this.abilities[i].hasCastTime()) {
                        return this.abilities[i];
                    }
                } */
            }
        }
        return null;
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
                this.targetExists = true;
            }
        } else targetExists = false;
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

    public double getParryChance() {
        return parryChance;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public double getBlockChance() {
        return blockChance;
    }

    /**
     * Performs All necessary calculations for the Combat event and calls the @target's defend function which handles application of damage reduction
     *
     * @param target  the target of the attack
     * @param ability the ability used for the attack
     */

    public void attack(EntityCharacter target, Ability ability) {
        Ability.abilityHitCategories hitType;
        double abilityDamage;
        //Global Cooldown
        if (ability.isOnGlobalCooldown()) {
            this.lastAbility = ability;
            if (!ability.hasCastTime()) {
                triggerGlobalCooldown();
            }
        }
        //Hit Calculation
        ability.use(this);
        if (ability.getAbilityCategory() == SPELL) {
            double critChance = this.criticalStrikeChance;
            double roll = Math.random();

            abilityDamage = this.spellPower * ability.getScaleFactor();

            if (roll < critChance) {
                hitType = ABILITY_CRIT;
            } else {
                hitType = ABILITY_HIT;
            }
        } else {
            double accuracy = this.accuracy;
            double missChance = this.missChance;
            double dodgeChance = target.getDodgeChance();
            double parryChance = target.getParryChance();
            double blockChance = target.getBlockChance();
            double critChance = this.criticalStrikeChance;
            double roll = Math.random();
            dodgeChance += (target.getLevel() - this.getLevel()) * dodgePerLevelDifference;
            parryChance += (target.getLevel() - this.getLevel()) * parryPerLevelDifference;

            if (accuracy > 0) {
                missChance -= accuracy;
                accuracy -= this.missChance;
                if (missChance < 0) missChance = 0;
            }
            if (accuracy > 0) {
                dodgeChance -= accuracy;
                accuracy -= target.getDodgeChance() + (target.getLevel() - this.getLevel()) * dodgePerLevelDifference;
                if (dodgeChance < 0) dodgeChance = 0;
            }
            if (accuracy > 0) {
                parryChance -= accuracy;
                if (parryChance < 0) parryChance = 0;
            }

            abilityDamage = 0;
            switch (ability.getAbilityCategory()) {
                case MELEE:
                    abilityDamage = this.attackPower * ability.getScaleFactor();
                    break;
                case WEAPON:
                    abilityDamage = Math.random() * (this.weaponDamageMax - this.weaponDamageMin) + this.weaponDamageMin;
                    abilityDamage *= ability.getScaleFactor();
                    abilityDamage *= 10;
                    abilityDamage = Math.round(abilityDamage);
                    abilityDamage /= 10;
                    break;
            }

            if (roll < missChance) {
                abilityDamage *= 0;
                hitType = ABILITY_MISS;
            } else if (roll < (missChance + dodgeChance)) {
                abilityDamage *= 0;
                hitType = ABILITY_DODGE;
            } else if (roll < (missChance + dodgeChance + parryChance)) {
                abilityDamage *= 0;
                hitType = ABILITY_PARRY;
            } else if (roll < (missChance + dodgeChance + parryChance + blockChance)) {
                hitType = ABILITY_BLOCK;
            } else if (roll < (missChance + dodgeChance + parryChance + blockChance + critChance)) {
                hitType = ABILITY_CRIT;
            } else {
                hitType = ABILITY_HIT;
            }
        }
        //TODO: Split Area of Effect damage on Main Target to allow lower damage on secondary Targets
        if (ability.hasAreaOfEffect() && !this.isEnemy) {
            if (ability.getAreaOfEffectLocation() == Ability.areaOfEffectLocations.TARGET) {
                for (EnemyCharacter enemyCharacter : IdleDungeoneer.idleDungeoneer.getEnemyCharacters()) {
                    if (target.getDistance(enemyCharacter) <= ability.getAreaOfEffectRange())
                        enemyCharacter.defend(this, ability, hitType, abilityDamage);
                }
            } else {
                for (EnemyCharacter enemyCharacter : IdleDungeoneer.idleDungeoneer.getEnemyCharacters()) {
                    if (this.getDistance(enemyCharacter) <= ability.getAreaOfEffectRange())
                        enemyCharacter.defend(this, ability, hitType, abilityDamage);
                }
            }
        } else {
            target.defend(this, ability, hitType, abilityDamage);
        }
        if (ability.appliesStatusEffect()) {
            for (Ability.abilityHitCategories type : ability.getStatusEffectApplicationCondition()) {
                if (hitType == type) {
                    switch (ability.getStatusEffectTarget()) {
                        case CASTER:
                            this.applyBuff(ability.getStatusEffect());
                            break;
                        case TARGET:
                            target.applyDebuff(ability.getStatusEffect());
                            break;
                    }
                }
            }
        }
    }

    public double defend(EntityCharacter attacker, Ability spell, Ability.abilityHitCategories hitType, double abilityDamage) {
        if (abilityDamage > 0) {
            double damageTaken = abilityDamage;
            switch (spell.getDamageType()) {
                case MAGIC:
                    if (hitType == ABILITY_CRIT) damageTaken *= 2;
                    damageTaken -= this.resistance;
                    if (damageTaken < 0) damageTaken = 0;
                    break;
                case PHYSICAL:
                    if (hitType == ABILITY_CRIT) damageTaken *= 2;
                    if (hitType == ABILITY_BLOCK) damageTaken -= this.blockAmount;
                    if (damageTaken < 0) damageTaken = 0;
                    damageTaken *= 1 - this.armorDamageReduction;
                    break;
            }
            this.health -= damageTaken;
            if (this.health <= 0) {
                this.health = 0;
                this.deathTime = Instant.now();
                this.dead = true;
            }
            this.generateAggro(attacker, damageTaken);
            if (hitType != null) {
                System.out.print(Instant.now().toString() + " " + attacker.getName() + " " + hitType + " ");
                if (damageTaken > 0) System.out.print(Math.round(damageTaken) + " ");
                System.out.println(this.getName());
            }
            return damageTaken;
        }
        return 0;
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

    public boolean hasBuff(StatusEffect statusEffect) {
        return this.currentBuffs.contains(statusEffect);
    }

    public void applyBuff(StatusEffect statusEffect) {
        if (this.currentBuffs.contains(statusEffect)) {
            this.currentBuffs.elementAt(this.currentBuffs.indexOf(statusEffect)).refresh();
        } else {
            this.currentBuffs.addElement(statusEffect);
            this.currentBuffs.lastElement().apply();
        }
    }

    public void removeBuff(StatusEffect statusEffect) {
        if (this.currentBuffs.contains(statusEffect)) this.currentBuffs.removeElement(statusEffect);
    }

    public void applyDebuff(StatusEffect statusEffect) {
        if (this.currentDebuffs.contains(statusEffect)) {
            this.currentDebuffs.elementAt(this.currentDebuffs.indexOf(statusEffect)).refresh();
        } else {
            this.currentDebuffs.addElement(statusEffect);
            this.currentDebuffs.lastElement().apply();
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
                }
            }
        }
    }

    public double getDistance(Entity e) {
        double xDistance = Math.abs(this.getPosition().x - e.getPosition().x);
        double yDistance = Math.abs(this.getPosition().y - e.getPosition().y);
        return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
    }

    public boolean inAbilityRange(Ability ability) {
        return (getDistance(this.getTarget()) <= ability.getRange());
    }

    public boolean moveToEntity(Entity e, long timeMoving, float range) {
        if (getDistance(e) > range) {
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

        if (getDistance(e) <= range) {
            return true;
        } else {
            return false;
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
        this.targetExists = true;
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
    }

    public boolean tick() {
        Instant now = Instant.now();
        long timeSinceLastTick = ChronoUnit.MILLIS.between(this.lastTick, now);
        this.lastTick = now;
        this.timedActions(timeSinceLastTick);
        this.ai();
        return true;
    }

    public void removeAggroTarget(EntityCharacter e) {
        if (!this.aggroTable.isEmpty()) {
            for (int i = 0; i < this.aggroTable.size(); i++) {
                if (this.aggroTable.elementAt(i).getTarget() == e) {
                    this.aggroTable.remove(i);
                    if (this.target == e) {
                        this.target = null;
                        this.targetExists = false;
                    }
                    break;
                }
            }
        }
        this.processAggro();
    }

    public void abilityHandling() {
        if (this.hasTarget() && !this.getTarget().isDead()) {
            if (this.characterStatus == WAITING || this.characterStatus == MOVING) {
                //Auto Hits
                if (abilities[0].cooldownReady() && this.abilities[0].isEnabled() && this.inAbilityRange(abilities[0])) {
                    attack(this.target, abilities[0]);
                }
                if (this.abilities[1] != null && this.abilities[1].isEnabled() && this.abilities[1].cooldownReady() && this.inAbilityRange(abilities[1])) {
                    attack(this.target, abilities[1]);
                }

                Ability nextCast = this.chooseNextCast();
                if (nextCast != null) {
                    if (nextCast.hasCastTime()) {
                        this.characterStatus = CASTING;
                        this.castingAbility = nextCast;
                        this.characterStatusUntil = Instant.now().plus(nextCast.getCastTime());
                        this.triggerGlobalCooldown();
                    } else {
                        attack(this.target, nextCast);
                    }
                }
            }
        } else {
            findTarget();
        }
    }

    public void ai() {
        //Character Status
        if (this.characterStatus != WAITING) {
            if (Instant.now().isAfter(this.characterStatusUntil)) {
                if (this.characterStatus == CASTING && this.target != null) {
                    attack(this.target, castingAbility);
                }
                this.characterStatus = WAITING;
            }
        }
        this.abilityHandling();
    }

    protected boolean hasTarget() {
        return targetExists;
    }

    public String getResourceName() {
        switch (resourceType) {
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
