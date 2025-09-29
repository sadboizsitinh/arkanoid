package arkanoid;
import javafx.scene.paint.Color;
public abstract class MovableObject extends GameObject {
    protected double dx;
    protected double dy;

    public MovableObject(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
        this.dx = 0;
        this.dy = 0;
    }

    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void update() {
        this.x += dx;
        this.y += dy;
    }

    public void stop() {
        this.dx = 0;
        this.dy = 0;
    }

    public void reverseX() {
        this.dx = -this.dx;
    }

    public void reverseY() {
        this.dy = -this.dy;
    }

    public boolean isMoving() {
        return dx != 0 || dy != 0;
    }

    public void setVelocity(double speed, double angle) {
        this.dx = speed * Math.cos(angle);
        this.dy = speed * Math.sin(angle);
    }

    public double getSpeed() {
        return Math.sqrt(dx * dx + dy * dy);
    }
}