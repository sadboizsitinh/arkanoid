package arkanoid.entities.Brick;

import javafx.scene.paint.Color;

/**
 * Normal brick - destroyed in one hit
 */
public class NormalBrick extends Brick {
    public NormalBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 1, BrickType.NORMAL);
        this.color = Color.ORANGE;
    }

    @Override
    public void updateColor() {
        // Normal bricks don't change color
    }
}
