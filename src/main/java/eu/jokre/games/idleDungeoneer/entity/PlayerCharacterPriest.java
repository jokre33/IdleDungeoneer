package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.IdleDungeoneer;
import eu.jokre.games.idleDungeoneer.Inventory.Item;
import eu.jokre.games.idleDungeoneer.ability.*;
import org.joml.Vector2d;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Vector;

import static eu.jokre.games.idleDungeoneer.ability.Ability.abilityHitCategories.ABILITY_CAST;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.characterStates.CASTING;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.characterStates.MOVING;
import static eu.jokre.games.idleDungeoneer.entity.EntityCharacter.characterStates.WAITING;

/**
 * Created by jokre on 22-May-17.
 */
public class PlayerCharacterPriest extends PlayerCharacter {

    private PlayerCharacter focusTarget;

    public PlayerCharacterPriest(int level, Vector2d position, String name) {
        super(level, position, name);
        this.armorClass = Item.armorClass.CLOTH;
        this.characterClass = characterClasses.PRIEST;
        this.abilities[0].disable();
        this.moveToRange = 6;
        this.addAbility(new AbilityPriestSmite(this), 2);
        this.addAbility(new AbilityPriestHeal(this), 20);
        this.addAbility(new AbilityPriestHealArea(this), 19);
        this.addAbility(new AbilityPriestRenew(this), 18);
        this.intelligence = 5500;
        this.spellPower = this.intelligence;
        this.maximumResource = this.intelligence * 5;
        this.resource = this.maximumResource;
        this.resourceRegeneration = this.maximumResource * 0.005;
        this.inventory.generateSampleGear(145, false);
        this.updateStats();

    }

    private Vector<PlayerCharacter> getHealingTargets(boolean percentHealth, double missingHealth) {
        Vector<PlayerCharacter> healingTargets = new Vector<>();
        if (percentHealth) {
            for (PlayerCharacter p : IdleDungeoneer.idleDungeoneer.getLivingPlayerCharacters()) {
                if ((p.getExpectedHealth() / p.getMaximumHealth()) < missingHealth) {
                    healingTargets.addElement(p);
                }
            }
        } else {
            for (PlayerCharacter p : IdleDungeoneer.idleDungeoneer.getLivingPlayerCharacters()) {
                if ((p.getMaximumHealth() - p.getExpectedHealth()) >= missingHealth) {
                    healingTargets.addElement(p);
                }
            }
        }
        return healingTargets;
    }

    private Vector<PlayerCharacter> getHealingTargets(boolean percentHealth, double missingHealth, Vector<PlayerCharacter> playerCharacter) {
        Vector<PlayerCharacter> healingTargets = new Vector<>();
        if (percentHealth) {
            for (PlayerCharacter p : playerCharacter) {
                if ((p.getExpectedHealth() / p.getMaximumHealth()) < missingHealth) {
                    healingTargets.addElement(p);
                }
            }
        } else {
            for (PlayerCharacter p : playerCharacter) {
                if ((p.getMaximumHealth() - p.getExpectedHealth()) >= missingHealth) {
                    healingTargets.addElement(p);
                }
            }
        }
        return healingTargets;
    }

    public boolean moveToTarget(long timeMoving) {
        if (this.focusTarget != null) {
            return moveToEntity(this.focusTarget, timeMoving, this.moveToRange);
        }
        return moveToEntity(this.getTarget(), timeMoving, this.moveToRange);
    }

    public boolean useAbility(PlayerCharacter t, Ability a) {
        if (this.globalCooldownRemaining() <= 0 && this.getResource() >= a.getCost()) {
            if (a.hasCastTime()) {
                if (this.characterStatus == WAITING) {
                    startCasting(t, a);
                    return true;
                }
            } else {
                heal(t, a);
                a.onCast(t);
                return true;
            }
        }
        return false;
    }

    public void startCasting(PlayerCharacter t, Ability a) {
        if (a.getTargetCategory() == Ability.targetCategories.FRIENDLIES) {
            if (!a.hasAreaOfEffect()) {
                t.addIncomingHealingSpell(a);
            } else {
                for (PlayerCharacter p : IdleDungeoneer.idleDungeoneer.getFriendlyTargetsInRange(t, a.getAreaOfEffectRange())) {
                    p.addIncomingHealingSpell(a);
                }
            }
        }
        this.characterStatus = CASTING;
        this.castingTarget = t;
        this.castingAbility = a;
        this.characterStatusUntil = Instant.now().plus(a.getCastTime());
        this.triggerGlobalCooldown();
    }

    public void handleHealingAbilities() {
        Vector<PlayerCharacter> friendlyTargetsInRange = IdleDungeoneer.idleDungeoneer.getFriendlyTargetsInRange(this, castRange);
        for (PlayerCharacter p : IdleDungeoneer.idleDungeoneer.getTanks()) {
            if (!p.isDead() && p.getExpectedHealth() / p.getMaximumHealth() < 0.4) {
                if (this.getHitboxDistance(p) <= castRange) {
                    if (this.useAbility(p, this.abilities[20])) return;
                } else {
                    this.focusTarget = p;
                }
            }
            if (!p.isDead() && !p.hasBuff(StatusEffectBuffRenew.class, this)) {
                if (this.useAbility(p, this.abilities[18])) return;
            }
        }
        if (getHealingTargets(false, this.getSpellPower() * this.abilities[19].getScaleFactor() * 0.75, friendlyTargetsInRange).size() > 2) {
            if (this.useAbility(this, this.abilities[19])) return;

        }
        for (PlayerCharacter p : IdleDungeoneer.idleDungeoneer.getLivingPlayerCharacters()) {
            if (p.getExpectedHealth() / p.getMaximumHealth() < 0.2) {
                if (this.getDistance(p) <= castRange) {
                    if (this.useAbility(p, this.abilities[20])) return;
                } else {
                    this.focusTarget = p;
                }
            }
        }
        for (PlayerCharacter p : getHealingTargets(true, 0.5, friendlyTargetsInRange)) {
            if (this.useAbility(p, this.abilities[20])) return;
        }
        for (PlayerCharacter p : getHealingTargets(false, this.getSpellPower() * this.abilities[20].getScaleFactor() * 0.75, friendlyTargetsInRange)) {
            if (this.useAbility(p, this.abilities[20])) return;
        }
        for (PlayerCharacter p : getHealingTargets(true, 1, friendlyTargetsInRange)) {
            if (!p.isDead() && !p.hasBuff(StatusEffectBuffRenew.class, this)) {
                if (this.useAbility(p, this.abilities[18])) return;
            }
            if (this.resource / this.maximumResource > 0.9) {
                if (this.useAbility(p, this.abilities[20])) return;
            }
        }
    }

    Ability chooseNextCast() {
        for (int i = 2; i < abilityCap; i++) { //Skip Slot 0 and 1 because Auto Hits get handled separately.
            if (this.abilities[i] != null && this.abilities[i].getTargetCategory() == Ability.targetCategories.ENEMIES) {
                if (this.abilities[i].cooldownReady() && this.resource >= this.abilities[i].getCost()) {
                    if (characterStatus == WAITING || (characterStatus == MOVING && !this.abilities[i].hasCastTime())) {
                        if (!this.abilities[i].isOnGlobalCooldown() || Instant.now().isAfter(this.globalCooldownUntil)) {
                            return this.abilities[i];
                        }
                    }
                }
            }
        }
        return null;
    }

    public void heal(EntityCharacter target, Ability ability) {
        if (ability.isOnGlobalCooldown()) {
            this.lastAbility = ability;
            if (!ability.hasCastTime()) {
                triggerGlobalCooldown();
            } else {
                target.removeIncomingHealingSpell(this);
            }
        }
        double healAmount = ability.getScaleFactor() * this.spellPower;

        if (ability.hasAreaOfEffect()) {
            if (ability.getAreaOfEffectLocation() == Ability.areaOfEffectLocations.TARGET) {
                for (PlayerCharacter p : IdleDungeoneer.idleDungeoneer.getPlayerCharacters()) {
                    if (target.getHitboxDistance(p) <= ability.getAreaOfEffectRange()) {
                        if (Math.random() < this.criticalStrikeChance) {
                            p.health += healAmount * 2;
                        } else {
                            p.health += healAmount;
                        }
                        if (p.health > p.maximumHealth) p.health = p.maximumHealth;
                        IdleDungeoneer.idleDungeoneer.generateAggroOnEnemyCharacters(this, healAmount / 2);
                    }
                }
            } else {
                for (PlayerCharacter p : IdleDungeoneer.idleDungeoneer.getPlayerCharacters()) {
                    if (this.getHitboxDistance(p) <= ability.getAreaOfEffectRange()) {
                        healAmount = ability.getScaleFactor() * this.spellPower;
                        if (Math.random() < this.criticalStrikeChance) {
                            p.health += healAmount * 2;
                        } else {
                            p.health += healAmount;
                        }
                        if (p.health > p.maximumHealth) p.health = p.maximumHealth;
                        IdleDungeoneer.idleDungeoneer.generateAggroOnEnemyCharacters(this, healAmount / 2);
                    }
                }
            }
        } else {
            if (Math.random() < this.criticalStrikeChance) {
                healAmount *= 2;
            }
            target.health += healAmount;
            if (target.health > target.maximumHealth) target.health = target.maximumHealth;
            IdleDungeoneer.idleDungeoneer.generateAggroOnEnemyCharacters(this, healAmount / 2);
        }
    }

    public void abilityHandling() {
        //Handle Healing here
        if (this.characterStatus == WAITING || this.characterStatus == MOVING) {
            this.handleHealingAbilities();
        }

        if (this.hasTarget() && !this.getTarget().isDead()) {
            if (this.characterStatus == WAITING || this.characterStatus == MOVING) {
                //Auto Hits
                if (abilities[0].cooldownReady() && this.abilities[0].isEnabled() && this.inAbilityRange(abilities[0])) {
                    attack(this.target, abilities[0]);
                }
                if (this.abilities[1] != null && this.abilities[1].isEnabled() && this.abilities[1].cooldownReady() && this.inAbilityRange(abilities[1])) {
                    attack(this.target, abilities[1]);
                }

                Ability nextCast = this.chooseNextCast();
                if (nextCast != null && this.getHitboxDistance(this.target) <= nextCast.getRange()) {
                    if (nextCast.hasCastTime()) {
                        this.characterStatus = CASTING;
                        this.castingAbility = nextCast;
                        this.castingTarget = this.target;
                        this.characterStatusUntil = Instant.now().plus(nextCast.getCastTime());
                        this.triggerGlobalCooldown();
                    } else {
                        attack(this.target, nextCast);
                    }
                }
            }
        } else {
            findTarget();
        }
    }

    public void findFocusTarget() {
        PlayerCharacter nearestTank = null;
        double nearestTankDistance = 1000;
        for (PlayerCharacter p : IdleDungeoneer.idleDungeoneer.getTanks()) {
            if (!p.isDead() && this.getDistance(p) < nearestTankDistance) {
                nearestTank = p;
                nearestTankDistance = this.getDistance(p);
            }
        }
        this.focusTarget = nearestTank;
    }

    public void ai() {
        if (this.characterStatus == CASTING) {
            if (this.castingTarget == null || this.castingTarget.isDead() || this.isDead()) {
                this.characterStatus = WAITING;
            }
        }
        if (this.focusTarget != null && this.focusTarget.isDead()) {
            this.focusTarget = null;
        }
        if (this.focusTarget == null) {
            findFocusTarget();
        } else
            //Character Status
            if (this.characterStatus != WAITING) {
                if (Instant.now().isAfter(this.characterStatusUntil)) {
                    if (this.characterStatus == CASTING) {
                        switch (castingAbility.getTargetCategory()) {
                            case ENEMIES:
                                if (this.castingTarget != null && !this.castingTarget.isDead()) {
                                    attack(this.castingTarget, castingAbility);
                                }
                                break;
                            case FRIENDLIES:
                                if (this.castingTarget != null && !this.castingTarget.isDead()) {
                                    heal(this.castingTarget, castingAbility);
                                }
                                break;
                        }
                    }
                    this.characterStatus = WAITING;
                }
            }
        this.abilityHandling();
    }

    @Override
    protected void abilityPriorityList() {
        //TODO: implement Priest DPS Spells
    }

    public boolean tick() {
        long timeSinceLastTick = ChronoUnit.MILLIS.between(this.lastTick, Instant.now());
        boolean _super = super.tick();
        if (this.characterStatus == WAITING && this.target != null) {
            this.moveToEntity(this.target, timeSinceLastTick, moveToRange);
        }
        return _super;
    }
}
