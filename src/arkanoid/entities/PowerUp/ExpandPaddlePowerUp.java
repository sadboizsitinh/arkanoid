package arkanoid.entities.PowerUp;

import arkanoid.entities.Paddle.PaddleLike;
import javafx.scene.paint.Color;

/**
 * Power-up that expands the paddle width
 */
public class ExpandPaddlePowerUp extends PowerUp {
    private static final double EXPANSION_MULTIPLIER = 1.5;
    private double originalWidth;

    public ExpandPaddlePowerUp(double x, double y) {
        super(x, y, PowerUpType.EXPAND_PADDLE, 10.0); // 10 second duration
        this.color = Color.GREEN;
    }

    @Override
    public void applyEffect(PaddleLike paddle) {
        if (!active) {
            originalWidth = paddle.getWidth();
            paddle.setWidth(originalWidth * EXPANSION_MULTIPLIER);
            activate();
        }
    }

    @Override
    public void removeEffect(PaddleLike paddle) {
        paddle.resetSize();
    }

    @Override
    protected String getSymbol() {
        return "E";
    }
}