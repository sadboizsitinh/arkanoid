package arkanoid.entities.Paddle;

import arkanoid.core.GameManager;
import arkanoid.entities.MovableObject;
import arkanoid.entities.PowerUp.PowerUp;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

/**
 * Player controlled paddle
 * Can move left and right, and apply power-ups
 * Supports sprite sheet textures
 * Includes frame animation on ball hit
 */
public class Paddle extends MovableObject implements PaddleLike {
    private static final double DEFAULT_WIDTH = 120;
    private static final double DEFAULT_HEIGHT = 36;
    private static final double DEFAULT_SPEED = 800;

    // Animation constants
    private static final double HIT_ANIMATION_DURATION = 0.2; // 0.5 seconds

    public int TypeSkin = 1;

    private PowerUp currentPowerUp;
    private double originalWidth;

    // Animation state
    private int currentFrame = 1; // 1 = paddle_1, 2 = paddle_2
    private double hitAnimationTimer = 0; // Timer để theo dõi thời gian animation
    private boolean isAnimating = false;

    public Paddle(double x, double y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED);

        this.originalWidth = DEFAULT_WIDTH;
        this.color = Color.BLUE;
        this.useTexture = false;
        this.currentFrame = 1;
        this.isAnimating = false;
        this.hitAnimationTimer = 0;

        loadPaddleTexture();
    }

    private void loadPaddleTexture() {
        String path = "file:src/arkanoid/assets/images/paddle_" + currentFrame + ".png";
        loadTexture(path);

        Image img = new Image(path);
        double W = img.getWidth();
        double H = img.getHeight();

        setSpriteRegion(0, 0, W, H);
    }

    /**
     * Gọi khi ball chạm vào paddle để bắt đầu animation
     */
    public void triggerHitAnimation() {
        isAnimating = true;
        hitAnimationTimer = 0;
        currentFrame = 2; // Chuyển sang paddle_2
        loadPaddleTexture();
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
    public void setHeight(double newHeight) {this.height = newHeight;}

    @Override
    public void update(double deltaTime) {
        // Cập nhật animation
        if (isAnimating) {
            hitAnimationTimer += deltaTime;

            if (hitAnimationTimer >= HIT_ANIMATION_DURATION) {
                // Animation kết thúc, chuyển lại về paddle_1
                isAnimating = false;
                hitAnimationTimer = 0;
                currentFrame = 1;
                loadPaddleTexture();
            }
        }

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