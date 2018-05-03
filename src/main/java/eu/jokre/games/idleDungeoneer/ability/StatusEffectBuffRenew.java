package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by jokre on 28-May-17.
 */
public class StatusEffectBuffRenew extends StatusEffectBuff {

    public StatusEffectBuffRenew(EntityCharacter owner, EntityCharacter target) {
        super(owner, target);
        this.healthChangePerTick = 0.5 * owner.getSpellPower();
        long tickTime = Math.round(1500 / (1 + owner.getHaste()));
        System.out.println(tickTime);
        long duration = Math.round(15000f / (float) tickTime) * tickTime + 1;
        this.tickTime = Duration.ofMillis(tickTime);
        this.duration = Duration.ofMillis(duration);

        this.expires = Instant.now().plus(this.duration);
        this.lastTick = Instant.now();
    }

    @Override
    public boolean tick() {
        if (Instant.now().isAfter(lastTick.plus(tickTime))) {
            this.lastTick = this.lastTick.plus(tickTime);
            this.target.getHealed(this.caster, this.healthChangePerTick);
        }
        return super.tick();
    }
}
