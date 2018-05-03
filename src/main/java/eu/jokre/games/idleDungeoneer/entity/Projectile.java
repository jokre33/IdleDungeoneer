package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.ability.Ability;
import org.joml.Vector2d;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by jokre on 28-May-17.
 */
public class Projectile extends Entity {
    private Ability containedSpell;
    private EntityCharacter target;
    private EntityCharacter caster;

    public Projectile(Vector2d position, float hitboxRadius, Ability containedSpell, EntityCharacter target, EntityCharacter caster, float speed) {
        super(position, hitboxRadius);
        this.containedSpell = containedSpell;
        this.target = target;
        this.caster = caster;
        this.movementSpeed = speed;
    }

    @Override
    public boolean tick() {
        Instant now = Instant.now();
        long tickTime = Duration.between(this.lastTick, now).toMillis();
        lastTick = now;
        if (target == null || target.isDead()) {
            return false;
        } else {
            moveToTarget(tickTime);
        }
        if (this.getHitboxDistance(target) <= 0) {
            caster.attack(this.target, this.containedSpell);
            return false;
        }
        return true;
    }

    @Override
    public void ai() {

    }

    private void moveToTarget(long timeMoving) {
        moveToEntity(this.target, timeMoving, 0);
    }
}
