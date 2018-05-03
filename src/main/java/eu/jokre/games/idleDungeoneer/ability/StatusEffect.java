package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by jokre on 20-May-17.
 */
public abstract class StatusEffect {
    protected Duration duration;
    protected Instant expires;
    protected Duration tickTime;
    protected Instant lastTick;
    protected double healthChangePerTick;
    protected String name;

    protected EntityCharacter caster;
    protected EntityCharacter target = null;

    public StatusEffect(EntityCharacter caster, EntityCharacter target) {
        this.caster = caster;
        this.target = target;
    }

    public Duration getRemainingTime() {
        return Duration.between(Instant.now(), this.expires);
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Instant getExpires() {
        return expires;
    }

    public void setExpires(Instant expires) {
        this.expires = expires;
    }

    public void refresh() {
        this.expires = Instant.now().plus(duration);
    }

    public Duration getTickTime() {
        return tickTime;
    }

    public double getHealthChangePerTick() {
        return healthChangePerTick;
    }

    public Instant getNextTick() {
        return lastTick.plus(tickTime);
    }

    public abstract boolean tick();

    public EntityCharacter getCaster() {
        return caster;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StatusEffect) {
            final StatusEffect statusEffect = (StatusEffect) o;
            if (statusEffect.getCaster() == this.caster && o.getClass() == this.getClass()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + 8 * caster.hashCode();
    }
}
