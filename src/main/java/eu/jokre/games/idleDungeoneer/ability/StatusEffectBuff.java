package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;

/**
 * Created by jokre on 22-May-17.
 */
public class StatusEffectBuff extends StatusEffect {
    public StatusEffectBuff(EntityCharacter caster, EntityCharacter target) {
        super(caster, target);
    }

    @Override
    public boolean tick() {
        return !this.getRemainingTime().isNegative();
    }
}
