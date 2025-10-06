package arkanoid;

import javafx.scene.image.Image;

/**
 * Abstract class for objects that can move
 * Extends GameObject with velocity properties
 */
public abstract class MovableObject extends GameObject {
    protected double dx, dy; // Velocity components
    protected double speed;

    public MovableObject(double x, double y, double width, double height, double speed) {
        super(x, y, width, height);
        this.speed = speed;
        this.dx = 0;
        this.dy = 0;
    }

    // Texture detail
    public Image spriteSheet;
    public double sourceX, sourceY, sourceWidth, sourceHeight;
    public boolean useTexture;

    /**
     * Load sprite sheet từ TextureManager
     */
    public void loadTexture(String resourcePath) {
        try {
            spriteSheet = TextureManager.getInstance().loadTexture(resourcePath);
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
     * Move the object based on its velocity
     */
    public void move(double deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
    }

    // Getters and Setters for velocity
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    public void setDx(double dx) { this.dx = dx; }
    public void setDy(double dy) { this.dy = dy; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
}
