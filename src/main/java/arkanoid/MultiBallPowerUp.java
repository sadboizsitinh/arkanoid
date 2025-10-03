package arkanoid;

import javafx.scene.paint.Color;

/**
 * Power-up that creates additional balls
 */
public class MultiBallPowerUp extends PowerUp {

    public MultiBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.MULTI_BALL, 0); // Instant effect
        this.color = Color.CYAN;
    }

    @Override
    public void applyEffect(PaddleLike paddle) {
    }

    @Override
    public void removeEffect(PaddleLike paddle) {
    }

    @Override
    protected String getSymbol() {
        return "M";
    }
}
