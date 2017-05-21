package eu.jokre.games.idleDungeoneer.ability;


import java.time.Duration;

/**
 * Created by jokre on 19-May-17.
 */

public class AutoAttack extends Ability {
    public AutoAttack() {
        this.setCooldown(Duration.ofMillis(1500));
        this.setAbilityCategory(abilityCategories.WEAPON);
    }
}
