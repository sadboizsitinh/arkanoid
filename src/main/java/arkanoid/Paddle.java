package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Player controlled paddle
 * Can move left and right, and apply power-ups
 */
public class Paddle extends MovableObject implements PaddleLike {
    private static final double DEFAULT_WIDTH = 100;
    private static final double DEFAULT_HEIGHT = 15;
    private static final double DEFAULT_SPEED = 800;

    private PowerUp currentPowerUp;
    private double originalWidth;

    public Paddle(double x, double y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED);
        this.originalWidth = DEFAULT_WIDTH;
        this.color = Color.BLUE;
    }

    public void moveLeft(double deltaTime) {
        dx = -speed;
        move(deltaTime);
        if (x < 0) x = 0; // Boundary check
        dx = 0;
    }

    public void moveRight(double deltaTime, double gameWidth) {
        dx = speed;
        move(deltaTime);
        if (x + width > gameWidth) x = gameWidth - width; // Boundary check
        dx = 0;
    }

    /**
     * Apply power-up effect to the paddle
     */
    public void applyPowerUp(PowerUp powerUp) {
        if (currentPowerUp != null) {
            currentPowerUp.removeEffect(this);
        }
        currentPowerUp = powerUp;
        powerUp.applyEffect(this);
    }

    public void resetSize() {
        this.width = originalWidth;
    }

    public void Update(double deltaTime, double gameWidth) {
        move(deltaTime); // thực hiện di chuyển dựa trên dx
        if (x < 0) x = 0;
        if (x + width > gameWidth) x = gameWidth - width;
        move(deltaTime);
        dx = 0;
    }


    @Override
    public void update(double deltaTime) {
        if (currentPowerUp != null && currentPowerUp.isExpired()) {
            currentPowerUp.removeEffect(this);
            currentPowerUp = null;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, width, height);
    }
}
