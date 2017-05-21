package eu.jokre.games.idleDungeoneer.ability;

import java.time.Duration;

/**
 * Created by jokre on 21-May-17.
 */
public class AutoAttackOffhand extends AutoAttack {
    public AutoAttackOffhand() {
        this.setCooldown(Duration.ofMillis(1500));
        this.setAbilityCategory(abilityCategories.WEAPON);
        this.enabled = false;
    }
}
