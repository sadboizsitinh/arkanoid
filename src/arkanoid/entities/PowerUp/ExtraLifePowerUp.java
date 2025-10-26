package arkanoid.entities.PowerUp;

import arkanoid.entities.Paddle.PaddleLike;
import javafx.scene.paint.Color;

/**
 * Grants an extra life instantly upon pickup.
 */
public class ExtraLifePowerUp extends PowerUp {
    public ExtraLifePowerUp(double x, double y) {
        super(x, y, PowerUpType.EXTRA_LIFE, 0); // instant
        this.color = Color.PINK;
    }

    @Override
    public void applyEffect(PaddleLike paddle) {
    }

    @Override
    public void removeEffect(PaddleLike paddle) {
    }

    @Override
    protected String getSymbol() {
        return "+"; // heart/plus indicator
    }
}
