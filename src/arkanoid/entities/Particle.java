package arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Individual particle for visual effects
 * Used for explosions, trails, and other visual enhancements
 */
public class Particle {
    private double x, y;
    private double dx, dy;
    private double life;
    private double maxLife;
    private Color color;
    private double size;
    private double gravity;
    private double alpha;

    public Particle(double x, double y, double dx, double dy, double life, Color color, double size) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.life = life;
        this.maxLife = life;
        this.color = color;
        this.size = size;
        this.gravity = 50; // Pixels per second squared
        this.alpha = 1.0;
    }

    public void update(double deltaTime) {
    }

    public void render(GraphicsContext gc) {
    }

    public boolean isDead() {
        return life <= 0;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
}
