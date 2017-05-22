package eu.jokre.games.idleDungeoneer.ability;

import java.time.Duration;

/**
 * Created by jokre on 22-May-17.
 */
public class StatusEffectBuffInstantFireball extends StatusEffectBuff {
    public StatusEffectBuffInstantFireball() {
        this.setDuration(Duration.ofSeconds(10));
    }
}
