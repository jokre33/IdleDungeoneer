package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.SPELL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityHitCategories.ABILITY_CRIT;
import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityHitCategories.ABILITY_HIT;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.MAGIC;
import static eu.jokre.games.idleDungeoneer.ability.Ability.statusEffectTargets.CASTER;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.ENEMIES;

/**
 * Created by jokre on 22-May-17.
 */
public class AbilitySmite extends Ability {
    public AbilitySmite(EntityCharacter owner) {
        super(owner);
        this.setRange(6);
        this.setCooldown(Duration.ZERO);
        this.setCost(0);
        this.setScaleFactor(1);
        this.setDamageType(MAGIC);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(ENEMIES);
        this.setCastTime(Duration.ofSeconds(2));
        this.setName("Smite");
        this.setOnGlobalCooldown(true);
        this.setAreaOfEffectRange(0.0f);
        this.setStatusEffect(new StatusEffectBuffInstantHeal());
        this.setStatusEffectTarget(CASTER);
        this.addStatusEffectApplicationCondition(ABILITY_CRIT);
        this.isOnGlobalCooldown = true;
    }
}
