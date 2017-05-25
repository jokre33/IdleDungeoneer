package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.ability.AutoAttack;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created by jokre on 20-May-17.
 */
public abstract class PlayerCharacter extends EntityCharacter {

    private boolean tank;

    public PlayerCharacter(int level, Vector2d position, String name) {
        super(level, 0, position, name, false);
        this.addAbility(new AutoAttack(this), 0);
        this.abilities[0].enable();
        this.addAbility(new AutoAttack(this), 1);
        this.abilities[1].disable();
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
        this.moveToRange = meleeRange;
        this.movementSpeed = 1;
        this.criticalStrikeChance = 0.5;
        this.setTank(false);
        this.updateStats();
    }

    @Override
    void updateStats() {

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
}
