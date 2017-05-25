package eu.jokre.games.idleDungeoneer.ability;

import java.time.Duration;

/**
 * Created by jokre on 22-May-17.
 */
public class StatusEffectBuffInstantHeal extends StatusEffectBuff {
    public StatusEffectBuffInstantHeal() {
        this.setDuration(Duration.ofSeconds(10));
    }
}
