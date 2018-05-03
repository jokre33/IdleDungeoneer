package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.ability.Ability;
import eu.jokre.games.idleDungeoneer.ability.AutoAttack;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static eu.jokre.games.idleDungeoneer.Inventory.Item.baseFullSetPrimary;
import static eu.jokre.games.idleDungeoneer.Inventory.Item.baseFullSetStamina;
import static eu.jokre.games.idleDungeoneer.Inventory.Item.itemLevelStatBase;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.resourceTypes.*;

/**
 * Created by jokre on 20-May-17.
 */
public class EnemyCharacter extends EntityCharacter {
    public enum Classification {
        NORMAL,
        ELITE,
        RARE_ELITE,
        DUNGEON_ELITE,
        DUNGEON_BOSS,
        RAID_ELITE,
        RAID_BOSS
    }

    double damageMod = 0;
    double healthMod = 0;

    public EnemyCharacter(int level, float itemlevel, Vector2d position, String name, Classification classification, float hitboxRadius) {
        super(level, itemlevel, position, name, true, hitboxRadius);
        switch (classification) {
            case NORMAL:
                damageMod = 0.6;
                healthMod = 1;
                break;
            case ELITE:
                damageMod = 0.75;
                healthMod = 2;
                break;
            case RARE_ELITE:
                damageMod = 1;
                healthMod = 3;
                this.level += 1;
                break;
            case DUNGEON_ELITE:
                damageMod = 1.5;
                healthMod = 5;
                this.level += 1;
                break;
            case DUNGEON_BOSS:
                damageMod = 1.8;
                healthMod = 20;
                this.level += 2;
                break;
            case RAID_ELITE:
                damageMod = 3;
                healthMod = 200;
                this.level += 3;
                break;
            case RAID_BOSS:
                damageMod = 6.5;
                healthMod = 500;
                this.level += 3;
                break;
            default:
                damageMod = 99999;
                healthMod = 99999;
                this.level += 99999;
                break;
        }
        this.addAbility(new AutoAttack(this), 0);
        this.abilities[0].enable();
        this.addAbility(new AutoAttack(this), 1);
        this.abilities[1].disable();
        this.updateStats();
        this.moveToRange = meleeRange;
        this.movementSpeed = 1;
    }

    @Override
    void updateStats() {
        this.canBlock = false;
        this.parryChance = parryBase;
        this.dodgeChance = dodgeBase;
        this.missChance = baseMissChance;
        this.armorRating = 2500;
        this.armorDamageReduction = this.armorRating / (this.armorRating + ratingConversion[this.level] * 50);
        this.stamina = (this.level + 10) * staminaGainPerLevel + Math.pow(itemLevelScaling, (this.itemlevel - itemLevelStatBase) / itemLevelScalingPerXAmount) * baseFullSetStamina;
        this.attackPower = (this.level + 10) * (statGainPerLevel + primaryStatBonus) + Math.pow(itemLevelScaling, (this.itemlevel - itemLevelStatBase) / itemLevelScalingPerXAmount) * baseFullSetPrimary;
        this.attackPower *= damageMod;
        this.spellPower = this.attackPower;
        this.maximumHealth = healthFromStamina * this.stamina * healthMod;
        this.health = maximumHealth;
        this.weaponDamageMin = this.attackPower * 0.95;
        this.weaponDamageMax = this.attackPower * 1.05;
        this.weaponAttackSpeed = Duration.ofMillis(1500);
        this.abilities[0].setCooldown(this.weaponAttackSpeed);
        this.dualWielding = false;
        this.maximumResource = 100 * this.level;
        this.resource = this.maximumResource;
        this.resourceRegeneration = this.maximumResource * 0.01; //Base regeneration. Will be modified for pretty much everything.
        this.resourceType = MANA;
    }

    public boolean tick() {
        if (!this.isDead()) {
            Instant now = Instant.now();
            long timeSinceLastTick = ChronoUnit.MILLIS.between(this.lastTick, now);
            this.lastTick = now;
            this.timedActions(timeSinceLastTick);
            IdleDungeoneer.idleDungeoneer.generateAggroOnPlayerCharacters(this, 1);
            this.ai();
        }
        return !this.isDead();
    }

    @Override
    protected void abilityPriorityList() {
        if (this.target != null) {
            for (Ability a : abilities) {
                if (a != null && a.getClass() != AutoAttack.class && useAbility(this.target, a)) return;
            }
        }
    }
}
