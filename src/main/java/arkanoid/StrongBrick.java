package arkanoid;

import javafx.scene.paint.Color;

/**
 * Strong Brick - chạm 3 lần mới chết
 */

public class StrongBrick extends Brick {
    public StrongBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 3, BrickType.STRONG);
        this.color = Color.ORANGE;
    }
}
