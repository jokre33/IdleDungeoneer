package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by jokre on 25-May-17.
 */
public class IncomingHealingAbility {
    private Ability ability;
    private Instant timeout;

    public IncomingHealingAbility(Ability a) {
        this.ability = a;
        this.timeout = Instant.now().plus(a.getCastTime()).plus(Duration.ofMillis(200));
    }

    public Ability getAbility() {
        return ability;
    }

    public Instant getTimeout() {
        return timeout;
    }
}
