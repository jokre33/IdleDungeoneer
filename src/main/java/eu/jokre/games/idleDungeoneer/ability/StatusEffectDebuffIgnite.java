package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;
import java.time.Instant;
import java.util.Vector;

/**
 * Created by jokre on 31-May-17.
 */
public class StatusEffectDebuffIgnite extends StatusEffectDebuff {

    private int ticks = 0;
    private int ticksRemaining = 0;
    private double damage;

    public StatusEffectDebuffIgnite(EntityCharacter caster, EntityCharacter target, double damage) {
        super(caster, target);
        this.damage = damage;
        this.tickTime = Duration.ofMillis(1000);
        this.ticks = 8;
        this.healthChangePerTick = this.damage / this.ticks;
        this.ticksRemaining = this.ticks;
        this.duration = tickTime.multipliedBy(this.ticks);
        this.expires = Instant.now().plus(duration);
        this.lastTick = Instant.now();
    }

    public StatusEffectDebuffIgnite copy() {
        StatusEffectDebuffIgnite ignite = new StatusEffectDebuffIgnite(this.caster, this.target, this.damage);
        ignite.ticksRemaining = this.ticksRemaining;
        ignite.healthChangePerTick = this.healthChangePerTick;
        ignite.expires = this.expires;
        ignite.lastTick = this.lastTick;
        return ignite;
    }

    @Override
    public boolean tick() {
        if (this.ticksRemaining > 0) {
            if (Instant.now().isAfter(lastTick.plus(tickTime))) {
                this.ticksRemaining -= 1;
                this.lastTick = this.lastTick.plus(tickTime);
                this.target.takeDamage(this.caster, this.healthChangePerTick);
                if (this.ticksRemaining > 0) {
                    Vector<EntityCharacter> targetAllies = target.getAlliesInRange(2);
                    if (!targetAllies.isEmpty()) {
                        for (EntityCharacter e : targetAllies) {
                            if (!e.hasDebuff(this)) {
                                e.applyDebuff(this.copy());
                                break;
                            }
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StatusEffect) {
            final StatusEffect statusEffect = (StatusEffect) o;
            if (statusEffect.getCaster() == this.caster && o.getClass() == this.getClass() && this.expires == statusEffect.expires) {
                return true;
            }
        }
        return false;
    }
}
