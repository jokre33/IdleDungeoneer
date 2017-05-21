package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.ability.AutoAttack;
import org.joml.Vector2f;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created by jokre on 20-May-17.
 */
public class PlayerCharacter extends EntityCharacter {
    public PlayerCharacter(int level, Vector2f position, String name) {
        super(level, 0, position, name, false);
        IdleDungeoneer.idleDungeoneer.generateAggroOnEnemyCharacters(this, 1);
        this.addAbility(new AutoAttack(), 0);
        this.addAbility(new AutoAttack(), 1);
        this.abilities[1].disable();
        this.weaponDamageMin = 2;
        this.weaponDamageMax = 3;
        this.spellPower = 30;
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

        if (this.isDead() && Instant.now().isAfter(this.deathTime.plus(Duration.ofSeconds(5)))) {
            this.dead = false;
            this.health = this.maximumHealth;
        }

        this.ai();
        return true;
    }
}
