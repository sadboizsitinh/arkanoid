package arkanoid.entities;

import javafx.scene.paint.Color;

/**
 * Power-up that increases ball speed
 */
public class FastBallPowerUp extends PowerUp {
    private static final double SPEED_MULTIPLIER = 1.5;

    public FastBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.FAST_BALL, 8.0); // 8 second duration
        this.color = Color.YELLOW;
    }

    @Override
    public void applyEffect(PaddleLike paddle) {
    }

    @Override
    public void removeEffect(PaddleLike paddle) {
    }

    @Override
    protected String getSymbol() {
        return "F";
    }
}
