package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

public class Ball {
    private double x, y, radius;
    private double dx, dy;

    public Ball(double x, double y, double radius, double dx, double dy) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.dx = dx; this.dy = dy;
    }

    public void update() {
        x += dx;
        y += dy;
        if (x - radius < 0 || x + radius > 800) bounceHorizontal();
        if (y - radius < 0 || y + radius > 600) bounceVertical();
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillOval(x - radius, y - radius, radius*2, radius*2);
    }

    public void handleCollision(Brick brick) {
        double left   = brick.getX();
        double right  = brick.getX() + brick.getWidth();
        double top    = brick.getY();
        double bottom = brick.getY() + brick.getHeight();
        // Phải đẩy ra trước khi nó bị va chạm vào 2 thẳng gạch
        // Bên trái
        if (x + radius > left && dx > 0 && x < left) {
            x = left - radius; // đẩy ra ngoài
            dx = -dx;
        }
        // Bên phải
        else if (x - radius < right && dx < 0 && x > right) {
            x = right + radius;
            dx = -dx;
        }
        // Bên trên
        else if (y + radius > top && dy > 0 && y < top) {
            y = top - radius;
            dy = -dy;
        }
        // Bên dưới
        else if (y - radius < bottom && dy < 0 && y > bottom) {
            y = bottom + radius;
            dy = -dy;
        }
    }


    boolean checkIntersects(GameObject brick) {
        double closestX = Math.max(brick.getX(), Math.min(x, brick.getX() + brick.getWidth()));
        double closestY = Math.max(brick.getY(), Math.min(y, brick.getY() + brick.getHeight()));

        double dx = x - closestX;
        double dy = y - closestY;

        return (dx * dx + dy * dy) <= (radius * radius);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(x - radius, y - radius, radius*2, radius*2);
    }

    public boolean intersects(Paddle paddle) {
        return getBounds().intersects(paddle.getBounds());
    }

    public void bounceVertical() { dy = -dy; }
    public void bounceHorizontal() { dx = -dx; }


}

