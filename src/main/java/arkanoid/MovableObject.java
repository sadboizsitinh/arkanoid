package arkanoid;

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
