package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

public class Ball {
    private double x, y, radius;
    private double dx, dy;

    public Ball(double x, double y, double radius, double dx, double dy) {
        this.x = x; this.y = y;
        this.radius = radius;
        this.dx = dx; this.dy = dy;
    }

    public void update() {
        x += dx;
        y += dy;
        if (x - radius < 0 || x + radius > 800) dx = -dx;
        if (y - radius < 0 || y + radius > 600) dy = -dy;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillOval(x - radius, y - radius, radius*2, radius*2);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(x - radius, y - radius, radius*2, radius*2);
    }

    public boolean intersects(Paddle paddle) {
        return getBounds().intersects(paddle.getBounds());
    }

    public void bounceVertical() { dy = -Math.abs(dy); }
}
