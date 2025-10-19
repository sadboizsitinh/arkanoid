package arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Base class for all brick types
 * Can be hit and destroyed
 */
public abstract class Brick extends GameObject {
    protected int hitPoints;
    protected int maxHitPoints;
    protected boolean destroyed;
    protected BrickType type;

    public enum BrickType {
        NORMAL, STRONG, UNBREAKABLE
    }

    public Brick(double x, double y, double width, double height, int hitPoints, BrickType type) {
        super(x, y, width, height);
        this.hitPoints = hitPoints;
        this.maxHitPoints = hitPoints;
        this.destroyed = false;
        this.type = type;
    }

    /**
     * Handle being hit by the ball
     */
    public void takeHit() {
        if (type != BrickType.UNBREAKABLE) {
            hitPoints--;
            if (hitPoints <= 0) {
                destroyed = true;
            }
            updateColor();
        }
    }

    public abstract void updateColor();

    public boolean isDestroyed() {
        return destroyed;
    }

    public BrickType getType() {
        return type;
    }

    public int getPoints() {
        switch (type) {
            case NORMAL:
                return 10;
            case STRONG:
                return 20;
            case UNBREAKABLE:
                return 0;
            default:
                return 0; // fallback an toàn
        }
    }


    @Override
    public void update(double deltaTime) {
        // Bricks don't need to update unless animated
    }

    @Override
    public void render(GraphicsContext gc) {
        String BrickCode = "";
        if (type == BrickType.NORMAL) {
            BrickCode = "normal_";
        }
        if (type == BrickType.STRONG) {
            BrickCode = "strong_";
        }
        if (type == BrickType.UNBREAKABLE) {
            BrickCode = "unbreakable_";
        }
        String path = "/brick_" + BrickCode + (hitPoints == 1 ? "2" : "1") + ".png";
        loadTexture(path);
        setSpriteRegion(0,0,384, 128);
        gc.drawImage(
                spriteSheet,
                sourceX, sourceY, sourceWidth, sourceHeight,
                x, y, width, height
        );
    }
    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int points) {
        this.hitPoints = points;
    }
}
