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
public class AbilityMageFireball extends Ability {
    public AbilityMageFireball(EntityCharacter owner) {
        super(owner);
        this.setRange(6);
        this.setCooldown(Duration.ZERO);
        this.setCost(50);
        this.setScaleFactor(1);
        this.setDamageType(MAGIC);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(ENEMIES);
        this.setCastTime(Duration.ofSeconds(2));
        this.setName("Fireball");
        this.setOnGlobalCooldown(true);
        this.setAreaOfEffectRange(0);
        this.isOnGlobalCooldown = true;
        this.projectile = true;
        this.projectileSpeed = 15f;
    }

    public boolean hasCastTime() {
        return !owner.hasBuff(StatusEffectBuffInstantFireball.class) && hasCastTime;
    }

    @Override
    public void onCast(EntityCharacter target) {
        super.onCast(target);
    }

    @Override
    public void onHit(EntityCharacter target) {
        //target.applyDebuff(new StatusEffectDebuffIgnite(this.owner, target, this.owner.getSpellPower() * this.getScaleFactor() * 0.5));
    }

    @Override
    public void onCrit(EntityCharacter target) {
        owner.applyBuff(new StatusEffectBuffInstantFireball(this.owner, this.owner));
        target.applyDebuff(new StatusEffectDebuffIgnite(this.owner, target, this.owner.getSpellPower() * this.getScaleFactor()));
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

    @Override
    protected void use() {
        super.use();
        this.owner.removeBuff(StatusEffectBuffInstantFireball.class);
    }
}
