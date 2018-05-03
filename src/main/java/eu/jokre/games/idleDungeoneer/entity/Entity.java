package eu.jokre.games.idleDungeoneer.entity;

import eu.jokre.games.idleDungeoneer.renderHelper.Image;
import org.joml.Vector2d;

import java.time.Instant;

/**
 * Created by jokre on 19-May-17.
 */

public abstract class Entity {
    Vector2d position = new Vector2d();
    Image texture;
    protected final float hitboxRadius;
    protected float movementSpeed;  //Movement Speed in Units/Second
    Instant lastTick = Instant.now();

    Entity(Vector2d position, float hitboxRadius) {
        this.position = position;
        this.hitboxRadius = hitboxRadius;
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

    public double getDistance(Entity e) {
        double xDistance = Math.abs(this.getPosition().x - e.getPosition().x);
        double yDistance = Math.abs(this.getPosition().y - e.getPosition().y);
        return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
    }

    public double getHitboxDistance(Entity e) {
        double xDistance = Math.abs(this.getPosition().x - e.getPosition().x);
        double yDistance = Math.abs(this.getPosition().y - e.getPosition().y);
        return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2)) - (this.getHitboxRadius() + e.getHitboxRadius());
    }

    public float getHitboxRadius() {
        return this.hitboxRadius;
    }

    public boolean moveToEntity(Entity e, long timeMoving, float range) {
        if (getHitboxDistance(e) > range) {
            double xDistance = e.getPosition().x - this.getPosition().x;
            double yDistance = e.getPosition().y - this.getPosition().y;
            double angle = Math.asin(Math.abs(xDistance) / getDistance(e));
            double moveX = Math.sin(angle) * this.movementSpeed * timeMoving / 1000;
            double moveY = Math.cos(angle) * this.movementSpeed * timeMoving / 1000;
            if (xDistance < 0) moveX *= -1;
            if (yDistance < 0) moveY *= -1;
            this.position.x += moveX;
            this.position.y += moveY;
        }

        if (getHitboxDistance(e) <= range) {
            return true;
        } else {
            return false;
        }
    }
}
