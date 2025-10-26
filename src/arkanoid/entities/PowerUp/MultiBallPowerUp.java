package arkanoid.entities.PowerUp;

import arkanoid.entities.Paddle.PaddleLike;
import javafx.scene.paint.Color;

/**
 * Power-up that creates additional balls
 * Spawns 2 extra balls that move in different directions
 */
public class MultiBallPowerUp extends PowerUp {

    public MultiBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.MULTI_BALL, 0); // Instant effect
        this.color = Color.CYAN;
    }

    @Override
    public void applyEffect(PaddleLike paddle) {
        // Effect is handled in GameManager through spawnMultiBalls()
        // This is an instant effect that doesn't need to track duration
    }

    @Override
    public void removeEffect(PaddleLike paddle) {
        // No effect to remove - balls persist until they fall off screen
    }

    @Override
    protected String getSymbol() {
        return "M";
    }

}