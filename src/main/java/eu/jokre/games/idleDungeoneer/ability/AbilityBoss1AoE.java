package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.SPELL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.MAGIC;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.ENEMIES;

/**
 * Created by jokre on 25-May-17.
 */
public class AbilityBoss1AoE extends Ability {
    public AbilityBoss1AoE(EntityCharacter owner) {
        super(owner);
        this.setRange(10);
        this.setCooldown(Duration.ofSeconds(10));
        this.setScaleFactor(0.5);
        this.setDamageType(MAGIC);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(ENEMIES);
        this.setName("Raid AoE");
        this.setAreaOfEffectLocation(areaOfEffectLocations.CASTER);
        this.setAreaOfEffectRange(10.0f);
    }
}
