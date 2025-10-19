package arkanoid.entities;

import javafx.scene.paint.Color;

/**
 * Strong brick - requires multiple hits to destroy
 */
public class StrongBrick extends Brick {
    public StrongBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 2, BrickType.STRONG);
        this.color = Color.DARKRED;
    }

    @Override
    public void updateColor() {
        // Change color based on remaining hit points
        if (hitPoints == 1) {
            this.color = Color.RED;
        } else {
            this.color = Color.DARKRED;
        }
    }
}