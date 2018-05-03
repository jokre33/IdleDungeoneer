package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.SPELL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityHitCategories.ABILITY_CAST;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.MAGIC;

/**
 * Created by jokre on 28-May-17.
 */
public class AbilityPriestRenew extends Ability {
    public AbilityPriestRenew(EntityCharacter owner) {
        super(owner);
        this.setRange(6);
        this.setCooldown(Duration.ZERO);
        this.setCost(400);
        this.setScaleFactor(0.1);
        this.setDamageType(MAGIC);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(targetCategories.FRIENDLIES);
        this.setCastTime(Duration.ZERO);
        this.setName("Renew");
        this.setOnGlobalCooldown(true);
        this.setAreaOfEffectRange(0.0f);
        this.isOnGlobalCooldown = true;
    }

    @Override
    public void onCast(EntityCharacter target) {
        super.onCast(target);
        target.applyBuff(new StatusEffectBuffRenew(this.owner, target));
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
