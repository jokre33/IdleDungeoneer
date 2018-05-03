package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

/**
 * Created by jokre on 31-May-17.
 */
public class StatusEffectDebuff extends StatusEffect {
    public StatusEffectDebuff(EntityCharacter caster, EntityCharacter target) {
        super(caster, target);
    }

    @Override
    public boolean tick() {
        return !this.getRemainingTime().isNegative();
    }
}
