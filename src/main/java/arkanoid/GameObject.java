package arkanoid;

import javafx.scene.canvas.GraphicsContext;
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
