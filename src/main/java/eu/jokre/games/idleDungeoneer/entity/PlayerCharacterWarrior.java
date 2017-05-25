package eu.jokre.games.idleDungeoneer.entity;

import org.joml.Vector2d;

import java.time.Duration;

/**
 * Created by jokre on 22-May-17.
 */
public class PlayerCharacterWarrior extends PlayerCharacter {
    public PlayerCharacterWarrior(int level, Vector2d position, String name) {
        super(level, position, name);
        this.abilities[0].enable();
        this.abilities[1].enable();
        this.setAggroModifier(10);
        this.attackPower = 5500;
        this.weaponDamageMin = attackPower * 0.95;
        this.weaponDamageMax = attackPower * 1.05;
        this.weaponAttackSpeed = Duration.ofMillis(2000);
        this.weapon2DamageMin = attackPower * 0.95;
        this.weapon2DamageMax = attackPower * 1.05;
        this.weapon2AttackSpeed = Duration.ofMillis(1800);
        this.abilities[0].setCooldown(weaponAttackSpeed);
        this.abilities[1].setCooldown(weapon2AttackSpeed);
        this.blockChance = 0.4;
        this.dodgeChance = 0.2;
        this.parryChance = 0.2;
        this.blockAmount = 10000;
        this.armorDamageReduction = 0.54;
        this.maximumHealth = this.stamina * 5 * 2;
        this.health = this.maximumHealth;
        this.setTank(true);
    }
}
