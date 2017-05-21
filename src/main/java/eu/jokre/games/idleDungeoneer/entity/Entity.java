package eu.jokre.games.idleDungeoneer.entity;

import org.joml.Vector2f;

/**
 * Created by jokre on 19-May-17.
 */

public abstract class Entity {
    Vector2f position = new Vector2f();

    public Entity(Vector2f position) {
        this.position = position;
    }

    public abstract boolean tick();

    public abstract void ai();
}
