package arkanoid;

import javafx.scene.paint.Color;

public class UnbreakableBrick extends Brick {
    public UnbreakableBrick(double x, double y, double width, double height) {
        super(x, y, width, height, Integer.MAX_VALUE, BrickType.UNBREAKABLE);
        this.color = Color.ORANGE;
    }
}
