package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by jokre on 22-May-17.
 */
public class StatusEffectBuffInstantHeal extends StatusEffectBuff {
    public StatusEffectBuffInstantHeal(EntityCharacter owner, EntityCharacter target) {
        super(owner, target);
        this.setDuration(Duration.ofSeconds(10));

        this.expires = Instant.now().plus(duration);
        this.lastTick = Instant.now();
    }
}
