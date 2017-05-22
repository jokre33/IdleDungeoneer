package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;
import java.time.Instant;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.SPELL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityHitCategories.ABILITY_CRIT;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.MAGIC;
import static eu.jokre.games.idleDungeoneer.ability.Ability.statusEffectTargets.CASTER;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.ENEMIES;

/**
 * Created by jokre on 20-May-17.
 */
public class AbilityFireball extends Ability {
    public AbilityFireball(EntityCharacter owner) {
        super(owner);
        this.setRange(6);
        this.setCooldown(Duration.ZERO);
        this.setCost(50);
        this.setScaleFactor(2);
        this.setDamageType(MAGIC);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(ENEMIES);
        this.setCastTime(Duration.ofSeconds(2));
        this.setName("Fireball");
        this.setOnGlobalCooldown(true);
        this.setAreaOfEffectRange(1.0f);
        this.setStatusEffect(new StatusEffectBuffInstantFireball());
        this.setStatusEffectTarget(CASTER);
        this.addStatusEffectApplicationCondition(ABILITY_CRIT);
        this.isOnGlobalCooldown = true;
    }

    public boolean hasCastTime() {
        if (owner.hasBuff(this.getStatusEffect())) return false;
        return hasCastTime;
    }

    public void use(EntityCharacter caster) {
        this.owner.removeBuff(this.getStatusEffect());
        this.availableAfter = Instant.now().plus(cooldown);
        caster.useResource(this.getCost());
    }
}
