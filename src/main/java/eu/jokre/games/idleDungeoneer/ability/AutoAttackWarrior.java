package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

/**
 * Created by jokre on 28-May-17.
 */
public class AutoAttackWarrior extends AutoAttack {
    public AutoAttackWarrior(EntityCharacter owner) {
        super(owner);
    }

    @Override
    public void onHit(EntityCharacter target) {
        owner.generateResource(15);
    }

    @Override
    public void onCrit(EntityCharacter target) {
        this.onHit(target);
    }

    @Override
    public void onBlock(EntityCharacter target) {
        this.onHit(target);
    }
}
