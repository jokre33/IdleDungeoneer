package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.Inventory.Item;
import eu.jokre.games.idleDungeoneer.ability.AbilityRogueStab;
import eu.jokre.games.idleDungeoneer.ability.AutoAttackRogue;
import org.joml.Vector2d;

import java.time.Duration;

/**
 * Created by jokre on 28-May-17.
 */
public class PlayerCharacterRogue extends PlayerCharacter {
    public PlayerCharacterRogue(int level, Vector2d position, String name) {
        super(level, position, name);
        this.armorClass = Item.armorClass.LEATHER;
        this.characterClass = characterClasses.ROGUE;
        this.abilities[0] = new AutoAttackRogue(this);
        this.abilities[1] = new AutoAttackRogue(this);
        this.abilities[0].enable();
        this.abilities[1].enable();
        this.attackPower = 5500;
        this.weaponDamageMin = attackPower * 0.95;
        this.weaponDamageMax = attackPower * 1.05;
        this.weaponAttackSpeed = Duration.ofMillis(1500);
        this.weapon2DamageMin = attackPower * 0.95;
        this.weapon2DamageMax = attackPower * 1.05;
        this.weapon2AttackSpeed = Duration.ofMillis(1200);
        this.abilities[0].setCooldown(weaponAttackSpeed);
        this.abilities[1].setCooldown(weapon2AttackSpeed);
        this.maximumHealth = this.stamina * 5;
        this.health = this.maximumHealth;
        this.resourceType = resourceTypes.ENERGY;
        this.maximumResource = 100;
        this.resource = this.maximumResource;
        this.resourceRegeneration = 10 * (1 + getHaste());
        this.addAbility(new AbilityRogueStab(this), 20);
        this.inventory.generateSampleGear(145, false);
        this.updateStats();
    }

    @Override
    protected void abilityPriorityList() {
        this.useAbility(this.target, this.abilities[20]);
    }
}
