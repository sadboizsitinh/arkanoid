package arkanoid.entities.Paddle;

import arkanoid.core.GameManager;
import arkanoid.entities.MovableObject;
import arkanoid.entities.PowerUp.PowerUp;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

public class Paddle extends MovableObject implements PaddleLike {
    private static final double DEFAULT_WIDTH = 120;
    private static final double DEFAULT_HEIGHT = 36;
    private static final double DEFAULT_SPEED = 800;

    private static final double HIT_ANIMATION_DURATION = 0.2;

    public int TypeSkin = 1;

    private PowerUp currentPowerUp;
    private double originalWidth;
    private double originalHeight;

    private int currentFrame = 1;
    private double hitAnimationTimer = 0;
    private boolean isAnimating = false;

    public Paddle(double x, double y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED);

        this.originalWidth = DEFAULT_WIDTH;
        this.originalHeight = DEFAULT_HEIGHT;
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

        try {
            Image img = new Image(path);
            double W = img.getWidth();
            double H = img.getHeight();
            setSpriteRegion(0, 0, W, H);
        } catch (Exception e) {
            // Texture loading failed, will use fallback
        }
    }

    public void triggerHitAnimation() {
        isAnimating = true;
        hitAnimationTimer = 0;
        currentFrame = 2;
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
        this.height = originalHeight;
    }

    public void setWidth(double newWidth) {
        this.width = newWidth;
    }

    public void setHeight(double newHeight) {
        this.height = newHeight;
    }

    @Override
    public void update(double deltaTime) {
        if (isAnimating) {
            hitAnimationTimer += deltaTime;

            if (hitAnimationTimer >= HIT_ANIMATION_DURATION) {
                isAnimating = false;
                hitAnimationTimer = 0;
                currentFrame = 1;
                loadPaddleTexture();
            }
        }

        if (currentPowerUp != null && currentPowerUp.isExpired()) {
            currentPowerUp.removeEffect(this);
            currentPowerUp = null;
        }
    }

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
        // ✅ TRY RENDER TEXTURE, NẾU FAIL → FALLBACK
        if (useTexture && spriteSheet != null && !spriteSheet.isError()) {
            gc.drawImage(
                    spriteSheet,
                    sourceX, sourceY, sourceWidth, sourceHeight,
                    x, y, width, height
            );
        } else {
            // ✅ FALLBACK: Vẽ paddle đơn giản
            renderPaddleFallback(gc);
        }
    }

    /**
     * ✅ Render paddle fallback khi texture không load được
     */
    private void renderPaddleFallback(GraphicsContext gc) {
        // Background gradient
        javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                x, y,
                x, y + height,
                false,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.web("#00BFFF")),
                new javafx.scene.paint.Stop(0.5, Color.web("#1E90FF")),
                new javafx.scene.paint.Stop(1, Color.web("#0080FF"))
        );

        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 8, 8);

        // Top highlight
        gc.setFill(Color.web("#87CEEB", 0.5));
        gc.fillRoundRect(x, y, width, height * 0.3, 8, 8);

        // Border
        gc.setStroke(Color.web("#87CEEB"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 8, 8);

        // Inner border (nếu đang hit animation)
        if (isAnimating) {
            gc.setStroke(Color.web("#FFFFFF", 0.7));
            gc.setLineWidth(1);
            gc.strokeRoundRect(x + 2, y + 2, width - 4, height - 4, 6, 6);
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
    public static double getDefaultHeight() {return DEFAULT_HEIGHT;}

    public void setX(double newX) {
        this.x = newX;
    }
    public void setY(double newY) {this.y = newY;}

    public double getSpeed() {
        return speed;
    }
}