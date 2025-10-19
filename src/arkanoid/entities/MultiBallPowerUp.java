package arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
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

    @Override
    public void render(GraphicsContext gc) {
        // Custom render with animated effect
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        // Draw multiple circles to represent multi-ball
        gc.setFill(Color.WHITE);
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double radius = 3;

        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        gc.fillOval(centerX - radius - 5, centerY - radius - 3, radius * 2, radius * 2);
        gc.fillOval(centerX - radius + 5, centerY - radius - 3, radius * 2, radius * 2);
    }
}