package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.WEAPON;
import static eu.jokre.games.idleDungeoneer.ability.Ability.areaOfEffectLocations.CASTER;
import static eu.jokre.games.idleDungeoneer.ability.Ability.areaOfEffectLocations.TARGET;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.PHYSICAL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.ENEMIES;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.meleeRange;

/**
 * Created by jokre on 28-May-17.
 */
public class AbilityWarriorSlam extends Ability {
    public AbilityWarriorSlam(EntityCharacter owner) {
        super(owner);
        this.setDamageType(PHYSICAL);
        this.setAbilityCategory(WEAPON);
        this.setTargetCategory(ENEMIES);
        this.setScaleFactor(0.5);
        this.setCost(0);
        this.setCastTime(Duration.ZERO);
        this.setAreaOfEffectRange(3f);
        this.setAreaOfEffectLocation(CASTER);
        this.setName("Slam");
        this.setCooldown(Duration.ZERO);
        this.setRange(meleeRange);
        this.hastedCooldown = true;
        this.setOnGlobalCooldown(true);
    }

    @Override
    public void onHit(EntityCharacter target) {
        owner.generateResource(2);
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
