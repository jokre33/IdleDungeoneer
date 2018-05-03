package eu.jokre.games.idleDungeoneer.ability;

import eu.jokre.games.idleDungeoneer.entity.EntityCharacter;

import java.time.Duration;
import java.time.Instant;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityCategories.SPELL;
import static eu.jokre.games.idleDungeoneer.ability.Ability.damageTypes.MAGIC;

/**
 * Created by jokre on 22-May-17.
 */
public class AbilityPriestHeal extends Ability {
    public AbilityPriestHeal(EntityCharacter owner) {
        super(owner);
        this.setRange(6);
        this.setCooldown(Duration.ZERO);
        this.setCost(400);
        this.setScaleFactor(2);
        this.setDamageType(MAGIC);
        this.setAbilityCategory(SPELL);
        this.setTargetCategory(targetCategories.FRIENDLIES);
        this.setCastTime(Duration.ofMillis(1500));
        this.setName("Heal");
        this.setOnGlobalCooldown(true);
        this.setAreaOfEffectRange(0.0f);
        this.isOnGlobalCooldown = true;
    }

    public boolean hasCastTime() {
        return !owner.hasBuff(StatusEffectBuffInstantHeal.class) && hasCastTime;
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

    @Override
    protected void use() {
        super.use();
        this.owner.removeBuff(StatusEffectBuffInstantHeal.class);
    }
}
