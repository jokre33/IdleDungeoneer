package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.SPELL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.WEAPON;
import static eu.jokre.games.idleDungeoneer.ability.Ability.areaOfEffectLocations.CASTER;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.PHYSICAL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.ENEMIES;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.meleeRange;

/**
 * Created by jokre on 28-May-17.
 */
public class AbilityWarriorTaunt extends Ability {
    public AbilityWarriorTaunt(EntityCharacter owner) {
        super(owner);
        this.setDamageType(PHYSICAL);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(ENEMIES);
        this.setScaleFactor(0);
        this.setCost(0);
        this.setCastTime(Duration.ZERO);
        this.setAreaOfEffectRange(0f);
        this.setAreaOfEffectLocation(CASTER);
        this.setName("Taunt");
        this.setCooldown(Duration.ofSeconds(4));
        this.setRange(999.0f);
        this.hastedCooldown = false;
        this.setOnGlobalCooldown(false);
    }

    @Override
    public void onHit(EntityCharacter target) {
        target.generateAggro(owner, owner.getAttackPower() * owner.getAggroModifier() * 1000);
    }

    @Override
    public void onCrit(EntityCharacter target) {

    }

    @Override
    public void onParry(EntityCharacter target) {

    }

    @Override
    public void onDodge(EntityCharacter target) {

    }

    @Override
    public void onBlock(EntityCharacter target) {

    }

    @Override
    public void onMiss(EntityCharacter target) {

    }
}
