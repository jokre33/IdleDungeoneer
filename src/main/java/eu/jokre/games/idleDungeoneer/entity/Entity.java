package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.renderHelper.Image;
import org.joml.Vector2d;

/**
 * Created by jokre on 19-May-17.
 */

public abstract class Entity {
    Vector2d position = new Vector2d();
    Image texture;

    Entity(Vector2d position) {
        this.position = position;
    }

    public abstract boolean tick();

    public abstract void ai();

    public Vector2d getPosition() {
        return position;
    }

    public void setTexture(Image texture) {
        this.texture = texture;
    }

    public void setTexture(String imagePath) {
        this.texture = new Image(imagePath);
    }

    public void draw() {
        if (texture != null) {
            texture.drawTexture(position);
        }
    }
}
