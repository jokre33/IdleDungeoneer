package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.ability.AbilityFireball;
import org.joml.Vector2d;

/**
 * Created by jokre on 22-May-17.
 */
public class PlayerCharacterMage extends PlayerCharacter {
    public PlayerCharacterMage(int level, Vector2d position, String name) {
        super(level, position, name);
        this.abilities[0].disable();
        this.moveToRange = 6;
        this.addAbility(new AbilityFireball(this), 20);
    }
}
