package eu.jokre.games.idleDungeoneer.ability;


import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.MELEE;
import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.WEAPON;
import static eu.jokre.games.idleDungeoneer.ability.Ability.areaOfEffectLocations.TARGET;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.PHYSICAL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.ENEMIES;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.meleeRange;

/**
 * Created by jokre on 19-May-17.
 */

public class AutoAttack extends Ability {
    public AutoAttack(EntityCharacter owner) {
        super(owner);
        this.setDamageType(PHYSICAL);
        this.setAbilityCategory(WEAPON);
        this.setTargetCategory(ENEMIES);
        this.setScaleFactor(1);
        this.setCost(0);
        this.setCastTime(Duration.ZERO);
        this.setAreaOfEffectRange(0);
        this.setAreaOfEffectLocation(TARGET);

        this.setCooldown(Duration.ofMillis(1500));
        this.setRange(meleeRange);
    }
}
