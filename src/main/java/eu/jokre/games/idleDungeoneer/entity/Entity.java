package eu.jokre.games.idleDungeoneer.entity;

import org.joml.Vector2d;

/**
 * Created by jokre on 19-May-17.
 */

public abstract class Entity {
    Vector2d position = new Vector2d();

    public Entity(Vector2d position) {
        this.position = position;
    }

    public abstract boolean tick();

    public abstract void ai();

    public Vector2d getPosition() {
        return position;
    }
}
