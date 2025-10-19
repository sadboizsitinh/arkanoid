package arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Enhanced paddle with additional features and power-up tracking
 */
public class EnhancedPaddle extends Paddle {
    private PowerUp currentPowerUp;

    public EnhancedPaddle(double x, double y) {
        super(x, y);
    }

    @Override
    public void applyPowerUp(PowerUp powerUp) {
        if (currentPowerUp != null) {
            currentPowerUp.removeEffect(this);
        }
        currentPowerUp = powerUp;
        powerUp.applyEffect(this);
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        // Update current power-up
        if (currentPowerUp != null) {
            currentPowerUp.update(deltaTime);
            if (currentPowerUp.isExpired()) {
                currentPowerUp.removeEffect(this);
                currentPowerUp = null;
            }
        }
    }

    public PowerUp getCurrentPowerUp() {
        return currentPowerUp;
    }

    public boolean hasPowerUp() {
        return currentPowerUp != null && !currentPowerUp.isExpired();
    }
}