package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

public class Paddle {
    private double x, y, width, height;

    public Paddle(double x, double y, double width, double height) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
    }

    public void move(double dx) {
        x += dx;
        if (x < 0) x = 0;
        if (x + width > 800) x = 800 - width;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLUE);
        gc.fillRect(x, y, width, height);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }
}
