package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.SPELL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.MAGIC;

/**
 * Created by jokre on 25-May-17.
 */
public class AbilityHealArea extends Ability {
    public AbilityHealArea(EntityCharacter owner) {
        super(owner);
        this.setRange(6);
        this.setCooldown(Duration.ZERO);
        this.setCost(1000);
        this.setScaleFactor(1);
        this.setDamageType(MAGIC);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(targetCategories.FRIENDLIES);
        this.setCastTime(Duration.ofMillis(2000));
        this.setName("Area Heal");
        this.setOnGlobalCooldown(true);
        this.setAreaOfEffectRange(6.0f);
        this.setAreaOfEffectLocation(areaOfEffectLocations.CASTER);
        this.isOnGlobalCooldown = true;
    }
}
