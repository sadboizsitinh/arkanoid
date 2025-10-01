package arkanoid;

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

    protected abstract void updateColor();

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
        if (!destroyed) {
            gc.setFill(color);
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x, y, width, height);
        }
    }
}
