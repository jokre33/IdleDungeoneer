package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.WEAPON;
import static eu.jokre.games.idleDungeoneer.ability.Ability.areaOfEffectLocations.CASTER;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.PHYSICAL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.targetCategories.ENEMIES;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.meleeRange;

/**
 * Created by jokre on 28-May-17.
 */
public class AbilityWarriorSlam2 extends Ability {
    public AbilityWarriorSlam2(EntityCharacter owner) {
        super(owner);
        this.setDamageType(PHYSICAL);
        this.setAbilityCategory(WEAPON);
        this.setTargetCategory(ENEMIES);
        this.setScaleFactor(2);
        this.setCost(40);
        this.setCastTime(Duration.ZERO);
        this.setAreaOfEffectRange(3f);
        this.setAreaOfEffectLocation(CASTER);
        this.setName("Slam 2");
        this.setCooldown(Duration.ZERO);
        this.setRange(meleeRange);
        this.hastedCooldown = true;
        this.setOnGlobalCooldown(true);
    }

    @Override
    public void onHit(EntityCharacter target) {

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
