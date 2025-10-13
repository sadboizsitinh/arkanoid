package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

/**
 * Player controlled paddle
 * Can move left and right, and apply power-ups
 * Supports sprite sheet textures
 */
public class Paddle extends MovableObject implements PaddleLike {
    private static final double DEFAULT_WIDTH = 100;
    private static final double DEFAULT_HEIGHT = 25;
    private static final double DEFAULT_SPEED = 800;

    private PowerUp currentPowerUp;
    private double originalWidth;

    public Paddle(double x, double y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED);

        this.originalWidth = DEFAULT_WIDTH;
        this.color = Color.BLUE;
        this.useTexture = false;


        loadTexture("/bricks-wip.png");
        setSpriteRegion(0, 125, 100, 25);
    }

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

    public void setWidth(double newWidth) {
        this.width = newWidth;
    }

    @Override
    public void update(double deltaTime) {
        // Progress power-up
        if (currentPowerUp != null && currentPowerUp.isExpired()) {
            currentPowerUp.removeEffect(this);
            currentPowerUp = null;
        }
    }

    // Update position and check bound
    public void moveLeft(double deltaTime) {
        x -= speed * deltaTime;

        if (x < 0) {
            x = 0;
        }
    }

    public void moveRight(double deltaTime) {
        x += speed * deltaTime;

        double gameWidth = GameManager.getInstance().getGameWidth();

        if (x + width > gameWidth) {
            x = gameWidth - width;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (useTexture && spriteSheet != null) {
            // Vẽ sprite từ sprite sheet
            // drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh)
            // sx, sy, sw, sh = source rectangle (vùng cắt từ sprite sheet)
            // dx, dy, dw, dh = destination rectangle (vị trí vẽ trên canvas)
            gc.drawImage(
                    spriteSheet,
                    sourceX, sourceY, sourceWidth, sourceHeight,  // Source rectangle
                    x, y, width, height                            // Destination rectangle
            );

        } else {
            // Fallback: vẽ bằng màu
            gc.setFill(color);
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x, y, width, height);
        }
    }

    // Getters
    public boolean isUsingTexture() {
        return useTexture;
    }

    public double getSourceX() { return sourceX; }
    public double getSourceY() { return sourceY; }
    public double getSourceWidth() { return sourceWidth; }
    public double getSourceHeight() { return sourceHeight; }
    public static double getDefaultWidth() {
        return DEFAULT_WIDTH;
    }
}