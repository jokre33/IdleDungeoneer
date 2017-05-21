package eu.jokre.games.idleDungeoneer.ability;

import java.time.Duration;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.SPELL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.MAGIC;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.ENEMIES;

/**
 * Created by jokre on 20-May-17.
 */
public class AbilityFireBall extends Ability {
    public AbilityFireBall() {
        this.setCooldown(Duration.ZERO);
        this.setCost(60);
        this.setScaleFactor(5);
        this.setDamageType(MAGIC);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(ENEMIES);
        this.setCastTime(Duration.ofSeconds(2));
    }
}
