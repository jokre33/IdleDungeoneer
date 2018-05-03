package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.Inventory.InventoryCharacter;
import eu.jokre.games.idleDungeoneer.Inventory.Item;
import eu.jokre.games.idleDungeoneer.ability.AutoAttack;
import org.joml.Vector2d;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created by jokre on 20-May-17.
 */
public abstract class PlayerCharacter extends EntityCharacter {

    protected InventoryCharacter inventory = new InventoryCharacter(this);

    public enum characterClasses {
        PRIEST,
        WARRIOR,
        ROGUE,
        MAGE
    }

    characterClasses characterClass;

    public void setLevel(int level) {
        this.level = level;
        this.updateStats();
    }

    public PlayerCharacter(int level, Vector2d position, String name) {
        super(level, 0, position, name, false, 0.5f);
        this.addAbility(new AutoAttack(this), 0);
        this.abilities[0].enable();
        this.addAbility(new AutoAttack(this), 1);
        this.abilities[1].disable();
        //Debug Start
        this.attackPower = 5500;
        this.weaponDamageMin = attackPower * 0.9;
        this.weaponDamageMin = attackPower * 1.1;
        this.weaponAttackSpeed = Duration.ofMillis(2000);
        this.weapon2DamageMin = attackPower * 0.9;
        this.weapon2DamageMin = attackPower * 1.1;
        this.weapon2AttackSpeed = Duration.ofMillis(1800);
        this.abilities[0].setCooldown(weaponAttackSpeed);
        this.abilities[1].setCooldown(weapon2AttackSpeed);
        this.weaponDamageMax = 3;
        this.spellPower = 5500;
        this.stamina = 5808;
        this.maximumHealth = this.stamina * 5;
        this.health = this.maximumHealth;
        this.maximumResource = 2000;
        this.resource = this.maximumResource;
        this.resourceRegeneration = this.maximumResource * 0.01;
        this.criticalStrikeChance = 0.5;
        //Debug End
        this.moveToRange = meleeRange;
        this.movementSpeed = 1;
        this.setTank(false);
        //this.updateStats();
    }

    @Override
    void updateStats() {
        switch (this.getCharacterClass()) {
            case WARRIOR:
                this.strength = (this.level + statBonusLevels) * (statGainPerLevel + primaryStatBonus);
                this.agility = (this.level + statBonusLevels) * statGainPerLevel;
                this.intelligence = (this.level + statBonusLevels) * statGainPerLevel;
                break;
            case ROGUE:
                this.strength = (this.level + statBonusLevels) * statGainPerLevel;
                this.agility = (this.level + statBonusLevels) * (statGainPerLevel + primaryStatBonus);
                this.intelligence = (this.level + statBonusLevels) * statGainPerLevel;
                break;
            case MAGE:
                this.strength = (this.level + statBonusLevels) * statGainPerLevel;
                this.agility = (this.level + statBonusLevels) * statGainPerLevel;
                this.intelligence = (this.level + statBonusLevels) * (statGainPerLevel + primaryStatBonus);
                break;
            case PRIEST:
                this.strength = (this.level + statBonusLevels) * statGainPerLevel;
                this.agility = (this.level + statBonusLevels) * statGainPerLevel;
                this.intelligence = (this.level + statBonusLevels) * (statGainPerLevel + primaryStatBonus);
                break;
        }
        this.stamina = (this.level + statBonusLevels) * staminaGainPerLevel;
        this.criticalStrikeRating = 0;
        this.hasteRating = 0;
        this.accuracyRating = 0;
        this.parryRating = 0;
        this.dodgeRating = 0;
        this.blockRating = 0;
        this.armorRating = 0;

        for (int i = 0; i < 16; i++) {
            Item item = inventory.getItem(i);
            if (item != null) {
                switch (item.getPrimaryStat()) {
                    case AGILITY:
                        this.agility += item.getPrimaryStatAmount();
                        break;
                    case STRENGTH:
                        this.strength += item.getPrimaryStatAmount();
                        break;
                    case INTELLIGENCE:
                        this.intelligence += item.getPrimaryStatAmount();
                        break;
                }
                this.stamina += item.getStaminaAmount();
                this.armorRating += item.getArmorAmount();

                switch (item.getSecondaryStat1()) {
                    case CRIT:
                        this.criticalStrikeRating += item.getSecondaryStat1Amount();
                        break;
                    case BLOCK:
                        this.blockRating += item.getSecondaryStat1Amount();
                        break;
                    case DODGE:
                        this.dodgeRating += item.getSecondaryStat1Amount();
                        break;
                    case HASTE:
                        this.hasteRating += item.getSecondaryStat1Amount();
                        break;
                    case PARRY:
                        this.parryRating += item.getSecondaryStat1Amount();
                        break;
                    case ACCURACY:
                        this.accuracyRating += item.getSecondaryStat1Amount();
                }

                switch (item.getSecondaryStat2()) {
                    case CRIT:
                        this.criticalStrikeRating += item.getSecondaryStat2Amount();
                        break;
                    case BLOCK:
                        this.blockRating += item.getSecondaryStat2Amount();
                        break;
                    case DODGE:
                        this.dodgeRating += item.getSecondaryStat2Amount();
                        break;
                    case HASTE:
                        this.hasteRating += item.getSecondaryStat2Amount();
                        break;
                    case PARRY:
                        this.parryRating += item.getSecondaryStat2Amount();
                        break;
                    case ACCURACY:
                        this.accuracyRating += item.getSecondaryStat2Amount();
                }

                if (item.getItemType() == Item.itemTypes.WEAPON_2H || (item.getItemType() == Item.itemTypes.WEAPON_1H && i == 14)) {
                    this.weaponDamageMin = item.getMinDamage();
                    this.weaponDamageMax = item.getMaxDamage();
                    this.weaponAttackSpeed = Duration.ofMillis(Math.round(item.getSwingTime() * 1000));
                }
                if (item.getItemType() == Item.itemTypes.WEAPON_1H && i == 15) {
                    this.weapon2DamageMin = item.getMinDamage();
                    this.weapon2DamageMax = item.getMaxDamage();
                    this.weapon2AttackSpeed = Duration.ofMillis(Math.round(item.getSwingTime() * 1000));
                }
            }
        }

        this.canBlock = inventory.getItem(15) != null && inventory.getItem(15).getItemType() == Item.itemTypes.SHIELD;
        if (this.isTank()) this.stamina *= 1 + tankStaminaBonus;

        this.attackPower = this.strength * attackPowerFromStrength + this.agility + attackPowerFromAgility;
        this.spellPower = this.intelligence * spellPowerFromIntelligence;

        this.criticalStrikeRating += this.agility * critFromAgility + this.intelligence * critFromIntelligence;
        this.blockRating += this.strength * blockFromStrength;
        this.dodgeRating += this.agility * dodgeFromAgility;
        this.parryRating += this.strength * parryFromStrength;

        this.maximumHealth = this.stamina * healthFromStamina;
        if (this.health < 0.5 * this.maximumHealth) this.health = this.maximumHealth * 0.5;
        if (this.resourceType == resourceTypes.MANA) this.maximumResource = this.intelligence * manaFromIntelligence;
        this.healthRegeneration = this.maximumHealth * 0.1;

        this.armorDamageReduction = this.armorRating / (this.armorRating + ratingConversion[this.level] * 50);

        this.blockChance = this.blockRating / (ratingConversion[this.level] * blockRatingConversionMultiplier + this.blockRating);
        this.parryChance = this.parryRating / (ratingConversion[this.level] * parryRatingConversionMultiplier + this.parryRating) + parryBase;
        this.dodgeChance = this.dodgeRating / (ratingConversion[this.level] * dodgeRatingConversionMultiplier + this.dodgeRating) + dodgeBase;
        this.criticalStrikeChance = this.criticalStrikeRating / (ratingConversion[this.level] * 100) + criticalStrikeChanceBase;
        this.accuracy = this.accuracyRating / (ratingConversion[this.level] * 100);
        this.haste = this.hasteRating / (ratingConversion[this.level] * 100);

        System.out.println(this.getName());
        System.out.println("Strength: " + this.strength);
        System.out.println("Stamina: " + this.stamina);
        System.out.println("Armor: " + this.armorRating);

        System.out.println("Parry Rating: " + this.parryRating + " (" + this.parryChance + ")");
        System.out.println("Dodge Rating: " + this.dodgeRating + " (" + this.dodgeChance + ")");
        System.out.println("Block Rating: " + this.blockRating + " (" + this.blockChance + ")");
        System.out.println("Total Avoid : " + (this.blockChance + this.parryChance + this.dodgeChance + baseMissChance));

        System.out.println("Crit : " + this.criticalStrikeRating + " (" + this.criticalStrikeChance + ")");
        System.out.println("Haste: " + this.hasteRating + " (" + this.haste + ")");
        System.out.println("Acc  : " + this.accuracyRating + " (" + this.accuracy + ")");
    }

    public boolean isTank() {
        return tank;
    }

    public void setTank(boolean t) {
        this.tank = t;
    }

    public boolean tick() {
        if (!this.isDead()) {
            Instant now = Instant.now();
            long timeSinceLastTick = ChronoUnit.MILLIS.between(this.lastTick, now);
            this.lastTick = now;
            this.timedActions(timeSinceLastTick);
            IdleDungeoneer.idleDungeoneer.generateAggroOnEnemyCharacters(this, 1);
            this.ai();
        }
        if (this.isDead()) {
            this.characterStatus = characterStates.WAITING;
            if (Instant.now().isAfter(this.deathTime.plus(Duration.ofSeconds(5)))) {
                this.incomingHealingSpells.clear();
                this.dead = false;
                this.health = this.maximumHealth;
            }
        }
        return true;
    }

    public characterClasses getCharacterClass() {
        return characterClass;
    }
}
