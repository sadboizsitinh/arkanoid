package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class Brick {
    private double x, y, width, height;
    private Color color;

    public Brick(double x, double y, double width, double height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);

        // Thêm viền trắng để gạch nổi bật hơn
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);
    }

    // Phương thức tạo danh sách gạch
    public static List<Brick> createBricks() {
        List<Brick> bricks = new ArrayList<>();

        double brickWidth = 75;
        double brickHeight = 20;
        double startX = 0;
        double startY = 0;

        // Tạo 5 hàng gạch với cùng một màu
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 10; col++) {
                double x = startX + col * (brickWidth + 5);
                double y = startY + row * (brickHeight + 5);
                bricks.add(new Brick(x, y, brickWidth, brickHeight, Color.RED));
            }
        }
        return bricks;
    }
}