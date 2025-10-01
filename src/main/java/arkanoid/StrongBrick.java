package arkanoid;

import javafx.scene.paint.Color;

/**
 * Strong brick - requires multiple hits to destroy
 */
public class StrongBrick extends Brick {
    public StrongBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 3, BrickType.STRONG);
        this.color = Color.RED;
    }

    @Override
    protected void updateColor() {
        double intensity = (double) hitPoints / maxHitPoints;
        this.color = Color.RED.interpolate(Color.DARKRED, 1 - intensity);
    }
}
