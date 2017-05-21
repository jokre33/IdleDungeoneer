package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.IDHelper;
import eu.jokre.games.idleDungeoneer.Inventory.Item;
import eu.jokre.games.idleDungeoneer.ability.Ability;
import eu.jokre.games.idleDungeoneer.ability.StatusEffect;
import org.joml.Vector2f;

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

public class EntityCharacter extends Entity {
    protected String name;

    public static final int abilityCap = 20;

    protected Ability[] abilities = new Ability[abilityCap];    //Any Entity can have and use up to 18 Active Abilities.
    protected Vector<Aggro> aggroTable = new Vector<Aggro>();   // > Slot 0 and 1 are reserved for Auto Hits.

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

    private characterStates characterStatus = WAITING;    //Defaults to waiting TODO: actually implement that LUL
    private Instant characterStatusUntil;                 //When is the Character available again
    private Ability castingAbility;                       //Used in conjunction with the CASTING status. When casting is done this ability will be used.

    private boolean isEnemy;

    /*********************
     * Stats and Scaling *
     *********************/

    public static final int maxLevel = 50;
    private int level;        //Increases Power of Player Characters and Enemy Characters alike.
    private float itemlevel;  //Only directly increases the Power of Enemy Characters. It acts as a gauging point for Player Character Power.

    public static final double itemLevelScaling = 0.15;
    public static final int itemLevelScalingPerXAmount = 15;

    public static final float[] ratingConversion = {0,
            51, 52, 53, 54, 55, 56, 57, 58, 59, 60,   //Level  1 - 10
            61, 62, 63, 64, 65, 66, 67, 68, 69, 70,   //Level 11 - 20
            71, 72, 73, 74, 75, 76, 77, 78, 79, 80,   //Level 21 - 30
            81, 82, 83, 84, 85, 86, 87, 88, 89, 90,   //Level 31 - 40
            91, 92, 93, 94, 95, 96, 97, 98, 99, 100   //Level 41 - 50
    };

    public static final int statGainPerLevel = 3;
    public static final int primaryStatBonus = 1;

    public static final int baseStatLevel = 10;

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
    double health;
    double maximumHealth;

    //Resources
    public enum resourceTypes {
        RAGE,
        MANA,
        ENERGY
    }

    private resourceTypes resourceType = MANA;
    double resource;
    double maximumResource;
    double resourceRegeneration;

    //Accuracy
    private double accuracy = 0;

    //Critical Chance / Damage
    protected double criticalStrikeRating;
    private double criticalStrikeChance;
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
    private double parryChance;
    public static final double parryBase = 0.05;
    public static final double parryPerLevelDifference = 0.01; //+1% per level above the Attacker -1% per Level below
    public static final double parryRatingConversionMultiplier = 0.75; //To offset the Quadratic growth a bit
    //TODO: Implement Parry & Dodge diminishing returns

    //Dodge
    protected double dodgeRating;
    private double dodgeChance;
    public static final double dodgeBase = 0.05;
    public static final double dodgePerLevelDifference = 0.01; //+1% per level above the Attacker -1% per Level below
    public static final double dodgeRatingConversionMultiplier = 0.75; //To offset the Quadratic growth a bit

    //Block
    protected double blockRating;
    protected double blockMultiplier;
    private double blockChance;
    protected double blockAmount;
    private boolean canBlock; //True if the Entity can Block
    public static final double blockRatingConversionMultiplier = 1.5;

    //Armor & Resistance
    protected double armorRating;
    protected double armorDamageReduction;
    protected double resistance;

    //Attack Power & Weapon
    private double attackPower;
    double weaponDamageMin;
    double weaponDamageMax;
    protected double weaponAttackSpeed;
    protected boolean dualWielding;     //Is the Character using a second Weapon in the Offhand. This adds a Second Auto Attack ability.
    protected double weapon2DamageMin;
    protected double weapon2DamageMax;
    protected double weapon2AttackSpeed;
    private double missChance;
    public static final double baseMissChance = 0.05;
    public static final double dualWieldMissChanceIncrease = 0.1;
    public static final double attackPowerConversionToWeaponDamage = 0.5;

    //Spell Power
    protected double spellPower;

    /****************/

    private Item[] items = new Item[16];

    protected Vector<StatusEffect> currentBuffs = new Vector<>();
    protected Vector<StatusEffect> currentDebuffs = new Vector<>();

    protected EntityCharacter target;

    boolean dead = false;
    private IDHelper helper = new IDHelper();
    private boolean targetExists;

    private Instant globalCooldownUntil = Instant.now();

    Instant deathTime = null;
    Instant lastTick = Instant.now();

    EntityCharacter(int level, float itemlevel, Vector2f position, String name, boolean isEnemy) {
        super(position);
        this.level = level;
        this.itemlevel = itemlevel;
        this.name = name;
        this.maximumHealth = 10 * level + 10 * itemlevel;
        this.health = this.maximumHealth;
        this.maximumResource = 100;
        this.resource = maximumResource;
        this.resourceRegeneration = maximumResource * 0.1;
        this.weaponDamageMin = level;
        this.weaponDamageMax = level * 1.2;
        this.isEnemy = isEnemy;
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
        //Hit Calculation
        ability.use(this);
        if (ability.getAbilityCategory() == SPELL) {
            double critChance = this.criticalStrikeChance;
            double roll = Math.random();

            double abilityDamage = this.spellPower * ability.getScaleFactor();

            if (roll < critChance) {
                target.defend(this, ability, ABILITY_CRIT, abilityDamage);
                hitType = ABILITY_CRIT;
            } else {
                target.defend(this, ability, ABILITY_HIT, abilityDamage);
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

            if (accuracy > 0) {
                missChance -= accuracy;
                accuracy -= this.missChance;
                if (missChance < 0) missChance = 0;
            }
            if (accuracy > 0) {
                dodgeChance -= accuracy;
                dodgeChance += (target.getLevel() - this.getLevel()) * dodgePerLevelDifference;
                accuracy -= target.getDodgeChance() + (target.getLevel() - this.getLevel()) * dodgePerLevelDifference;
                if (dodgeChance < 0) dodgeChance = 0;
            }
            if (accuracy > 0) {
                parryChance -= accuracy;
                parryChance += (target.getLevel() - this.getLevel()) * parryPerLevelDifference;
                if (parryChance < 0) parryChance = 0;
            }

            double abilityDamage = 0;
            switch (ability.getAbilityCategory()) {
                case MELEE:
                    abilityDamage = this.attackPower * ability.getScaleFactor();
                    break;
                case WEAPON:
                    abilityDamage = Math.random() * (this.weaponDamageMax - this.weaponDamageMin) + this.weaponDamageMin;
                    abilityDamage *= 10;
                    abilityDamage = Math.round(abilityDamage);
                    abilityDamage /= 10;
                    abilityDamage *= ability.getScaleFactor();
                    break;
            }

            if (roll < missChance) {
                target.defend(this, ability, ABILITY_MISS, 0);
                hitType = ABILITY_MISS;
            } else if (roll < (missChance + dodgeChance)) {
                target.defend(this, ability, ABILITY_DODGE, 0);
                hitType = ABILITY_DODGE;
            } else if (roll < (missChance + dodgeChance + parryChance)) {
                target.defend(this, ability, ABILITY_PARRY, 0);
                hitType = ABILITY_PARRY;
            } else if (roll < (missChance + dodgeChance + parryChance + blockChance)) {
                target.defend(this, ability, ABILITY_BLOCK, abilityDamage);
                hitType = ABILITY_BLOCK;
            } else if (roll < (missChance + dodgeChance + parryChance + blockChance + critChance)) {
                target.defend(this, ability, ABILITY_CRIT, abilityDamage);
                hitType = ABILITY_CRIT;
            } else {
                target.defend(this, ability, ABILITY_HIT, abilityDamage);
                hitType = ABILITY_HIT;
            }
        }
        //TODO: Handle Buff application
    }

    public double defend(EntityCharacter attacker, Ability spell, Ability.abilityHitCategories hitType, double abilityDamage) {
        if (spell.appliesStatusEffect() && spell.getStatusEffectTarget() == TARGET) {
            boolean applyDebuff = false;

            for (Ability.abilityHitCategories hitConditional : spell.getStatusEffectApplicationCondition()) {
                if (hitConditional == hitType) {
                    applyDebuff = true;
                    break;
                }
            }

            if (!currentDebuffs.contains(spell.getStatusEffect()) && applyDebuff)
                currentDebuffs.addElement(spell.getStatusEffect());
            currentDebuffs.elementAt(currentDebuffs.indexOf(spell.getStatusEffect())).apply();
        }

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
            return damageTaken;
        }
        return 0;
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

    public boolean tick() {
        Instant now = Instant.now();
        long timeSinceLastTick = ChronoUnit.MILLIS.between(this.lastTick, now);
        this.lastTick = now;

        if (this.resource < this.maximumResource) {
            this.resource += timeSinceLastTick * resourceRegeneration / 1000;
        }
        if (this.resource > this.maximumResource) {
            this.resource = this.maximumResource;
        }

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
                if (abilities[0].cooldownReady()) {
                    attack(this.target, abilities[0]);
                }
                if (this.abilities[1] != null && this.abilities[1].isEnabled() && this.abilities[1].cooldownReady()) {
                    attack(this.target, abilities[1]);
                }

                Ability nextCast = this.chooseNextCast();
                if (nextCast != null) {
                    if (nextCast.hasCastTime()) {
                        this.characterStatus = CASTING;
                        this.castingAbility = nextCast;
                        this.characterStatusUntil = Instant.now().plus(nextCast.getCastTime());
                    } else {
                        attack(this.target, nextCast);
                    }
                }
            }
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
