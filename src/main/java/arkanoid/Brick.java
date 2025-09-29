package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class Brick extends GameObject {
    protected int hitPoints;
    protected int maxHitPoints;
    protected boolean destroyed;
    protected BrickType type;

    public Brick(double x, double y, double width, double height, int hitPoints, BrickType type) {
        super(x, y, width, height);
        this.hitPoints = hitPoints;
        this.maxHitPoints = hitPoints;
        this.destroyed = false;
        this.type = type;
    }

    public enum BrickType {
        NORMAL, STRONG, UNBREAKABLE;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public BrickType getType() {
        return type;
    }

    public void takeHit() {
        if (type != BrickType.UNBREAKABLE) {
            hitPoints--;
            if (hitPoints <= 0) {
                destroyed = true;
            }
            // update color khi bị giảm máu
        }
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
                BrickFactory brickFactory = new BrickFactory();
                bricks.add(brickFactory.createRandomBrick(x, y, brickWidth, brickHeight));
            }
        }
        return bricks;
    }
}