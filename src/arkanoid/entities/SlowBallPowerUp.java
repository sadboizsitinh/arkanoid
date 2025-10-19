package arkanoid.entities;

import javafx.scene.paint.Color;

/**
 * Temporarily slows the ball for more control.
 */
public class SlowBallPowerUp extends PowerUp {
    private static final double SLOW_MULTIPLIER = 0.7;

    public SlowBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.SLOW_BALL, 6.0);
        this.color = Color.VIOLET;
    }

    @Override
    public void applyEffect(PaddleLike paddle) {

    }

    @Override
    public void removeEffect(PaddleLike paddle) {

    }

    @Override
    protected String getSymbol() {return "S";}
}
