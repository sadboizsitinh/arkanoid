package arkanoid.entities;

import arkanoid.utils.TextureManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Abstract base class for all game objects
 * Implements basic position and size properties
 */
public abstract class GameObject {
    protected double x, y;
    protected double width, height;
    protected Color color;

    public GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = Color.WHITE;
    }

    // Texture detail
    public Image spriteSheet;
    public double sourceX, sourceY, sourceWidth, sourceHeight;
    public boolean useTexture;


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

    public void setSpriteRegion(double x, double y, double width, double height) {
        this.sourceX = x;
        this.sourceY = y;
        this.sourceWidth = width;
        this.sourceHeight = height;
    }

    // Getters and Setters with proper encapsulation
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }

    /**
     * Check collision with another GameObject
     */
    public boolean intersects(GameObject other) {
        return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y;
    }

    // Abstract methods to be implemented by subclasses
    public abstract void update(double deltaTime);
    public abstract void render(GraphicsContext gc);
}
