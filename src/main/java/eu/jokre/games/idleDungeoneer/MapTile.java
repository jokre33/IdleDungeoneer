package eu.jokre.games.idleDungeoneer;

import org.joml.Vector2f;

/**
 * Created by jokre on 22-May-17.
 */
public class MapTile {
    private Vector2f position;

    public MapTile(Vector2f position) {
        this.position = position;
    }

    public Vector2f getPosition() {
        return position;
    }
}
