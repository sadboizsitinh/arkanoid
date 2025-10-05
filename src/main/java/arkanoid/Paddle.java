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

    // Texture properties
    private Image spriteSheet;
    private double sourceX, sourceY, sourceWidth, sourceHeight;
    private boolean useTexture;

    public Paddle(double x, double y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED);
        this.originalWidth = DEFAULT_WIDTH;
        this.color = Color.BLUE;
        this.useTexture = false;

        // Set sprite coordinates
        this.sourceX = 0;
        this.sourceY = 100;
        this.sourceWidth = 75;
        this.sourceHeight = 25;

        loadTexture();
    }

    /**
     * Load sprite sheet từ TextureManager
     */
    private void loadTexture() {
        try {
            spriteSheet = TextureManager.getInstance().loadTexture("/bricks-wip.png");
            if (spriteSheet != null) {
                useTexture = true;
                System.out.println("Paddle sprite sheet loaded via TextureManager!");
            } else {
                useTexture = false;
                System.out.println("Sprite sheet not found, using color fallback");
            }
        } catch (Exception e) {
            System.out.println("Could not load sprite sheet: " + e.getMessage());
            useTexture = false;
        }
    }

    /**
     * Set custom sprite sheet qua TextureManager
     * @param resourcePath Đường dẫn trong resources (vd: "/images/my_sprites.png")
     */
    public void setSpriteSheetFromResources(String resourcePath) {
        spriteSheet = TextureManager.getInstance().loadTexture(resourcePath);
        useTexture = (spriteSheet != null);
        if (useTexture) {
            System.out.println("Paddle sprite sheet changed to: " + resourcePath);
        }
    }

    /**
     * Set sprite sheet từ file system qua TextureManager
     * @param filePath Đường dẫn file (vd: "C:/images/sprite.png")
     */
    public void setSpriteSheetFromFile(String filePath) {
        spriteSheet = TextureManager.getInstance().loadTextureFromFile(filePath);
        useTexture = (spriteSheet != null);
        if (useTexture) {
            System.out.println("Paddle sprite sheet loaded from file: " + filePath);
        }
    }

    /**
     * Đặt vùng sprite cần lấy từ sprite sheet
     * @param x Tọa độ X trên sprite sheet
     * @param y Tọa độ Y trên sprite sheet
     * @param width Chiều rộng vùng cắt
     * @param height Chiều cao vùng cắt
     */
    public void setSpriteRegion(double x, double y, double width, double height) {
        this.sourceX = x;
        this.sourceY = y;
        this.sourceWidth = width;
        this.sourceHeight = height;
    }



    /**
     * Toggle texture on/off
     */
    public void toggleTexture() {
        useTexture = !useTexture && spriteSheet != null;
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
        dx = -speed;

        move(deltaTime);

        if (x < 0) x = 0;
        dx = 0;
    }

    public void moveRight(double deltaTime) {
        dx = speed;

        x += dx * deltaTime;

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
            setSpriteRegion(0,125,100,25);
            gc.drawImage(
                    spriteSheet,
                    sourceX, sourceY, sourceWidth, sourceHeight,  // Source rectangle
                    x, y, width, height                            // Destination rectangle
            );

            // Optional: Viền đen
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, width, height);
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
}