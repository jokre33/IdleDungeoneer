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
public class AbilityMageBigFireball extends Ability {
    public AbilityMageBigFireball(EntityCharacter owner) {
        super(owner);
        this.setRange(6);
        this.setCooldown(Duration.ofSeconds(5));
        this.setCost(50);
        this.setScaleFactor(4);
        this.setDamageType(MAGIC);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(ENEMIES);
        this.setCastTime(Duration.ofSeconds(2));
        this.setName("Big Fireball");
        this.setOnGlobalCooldown(true);
        this.setAreaOfEffectRange(1.0f);
        this.isOnGlobalCooldown = true;
        this.projectile = true;
        this.projectileSpeed = 5.0f;
        this.projectileSize = 0.8f;
    }

    @Override
    public void onCast(EntityCharacter target) {
        super.onCast(target);
    }

    @Override
    public void onHit(EntityCharacter target) {
        //target.applyDebuff(new StatusEffectDebuffIgnite(this.owner, target, this.owner.getSpellPower() * this.getScaleFactor()));
    }

    @Override
    public void onCrit(EntityCharacter target) {
        owner.applyBuff(new StatusEffectBuffInstantFireball(this.owner, this.owner));
        target.applyDebuff(new StatusEffectDebuffIgnite(this.owner, target, this.owner.getSpellPower() * this.getScaleFactor() * 2));
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
