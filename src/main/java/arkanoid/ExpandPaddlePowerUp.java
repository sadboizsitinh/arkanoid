package arkanoid;

import javafx.scene.paint.Color;

/**
 * Power-up that expands the paddle size
 */
public class ExpandPaddlePowerUp extends PowerUp {
    private static final double EXPANSION_FACTOR = 1.5;

    public ExpandPaddlePowerUp(double x, double y) {
        super(x, y, PowerUpType.EXPAND_PADDLE, 10.0); // 10 second duration
        this.color = Color.GREEN;
    }

    @Override
    public void applyEffect(PaddleLike paddle) {
    }

    @Override
    public void removeEffect(PaddleLike paddle) {

    }

    @Override
    protected String getSymbol() {
        return "E";
    }
}
