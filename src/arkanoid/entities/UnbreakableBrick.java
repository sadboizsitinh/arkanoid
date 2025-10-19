package arkanoid.entities;

import javafx.scene.paint.Color;

/**
 * Unbreakable brick - cannot be destroyed
 */
public class UnbreakableBrick extends Brick {
    public UnbreakableBrick(double x, double y, double width, double height) {
        super(x, y, width, height, Integer.MAX_VALUE, BrickType.UNBREAKABLE);
        this.color = Color.GRAY;
    }

    @Override
    public void updateColor() {
        // Unbreakable bricks don't change color
    }

    @Override
    public void takeHit() {
        // Cannot be destroyed - do nothing
    }
}
