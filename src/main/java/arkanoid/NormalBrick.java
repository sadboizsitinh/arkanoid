package arkanoid;

import javafx.scene.paint.Color;

/**
 * Normal brick - chết khi bóng chạm
 */
public class NormalBrick extends Brick {
    public NormalBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 1, BrickType.NORMAL);
        this.color = Color.RED;
    }

}