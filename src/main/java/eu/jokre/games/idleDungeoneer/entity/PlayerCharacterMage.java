package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.Inventory.Item;
import eu.jokre.games.idleDungeoneer.ability.AbilityMageBigFireball;
import eu.jokre.games.idleDungeoneer.ability.AbilityMageFireball;
import eu.jokre.games.idleDungeoneer.ability.StatusEffectBuffInstantFireball;
import org.joml.Vector2d;

import static eu.jokre.games.idleDungeoneer.entity.PlayerCharacter.characterClasses.MAGE;

/**
 * Created by jokre on 22-May-17.
 */
public class PlayerCharacterMage extends PlayerCharacter {
    public PlayerCharacterMage(int level, Vector2d position, String name) {
        super(level, position, name);
        this.armorClass = Item.armorClass.CLOTH;
        this.characterClass = MAGE;
        this.abilities[0].disable();
        this.moveToRange = 6;
        this.addAbility(new AbilityMageBigFireball(this), 19);
        this.addAbility(new AbilityMageFireball(this), 20);
        this.inventory.generateSampleGear(145, false);
        this.updateStats();
        this.resourceRegeneration = this.maximumResource * 0.01;
    }

    @Override
    protected void abilityPriorityList() {
        if (this.hasBuff(StatusEffectBuffInstantFireball.class)) this.useAbility(this.target, this.abilities[20]);
        this.useAbility(this.target, this.abilities[19]);
        this.useAbility(this.target, this.abilities[20]);
    }
}
