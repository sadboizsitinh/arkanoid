package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class GameManager {
    private final GraphicsContext gc;
    private final Ball ball;
    private final Paddle paddle;
    private final List<Brick> bricks;
    private final Set<KeyCode> keys = new HashSet<>();

    public GameManager(GraphicsContext gc) {
        this.gc = gc;
        ball = new Ball(400, 300, 10, 4, 4);
        paddle = new Paddle(350, 550, 100, 15);
        bricks = Brick.createBricks();
    }

    public void onKeyPressed(KeyCode code) { keys.add(code); }
    public void onKeyReleased(KeyCode code) { keys.remove(code); }

    public void update() {
        // di chuyển paddle
        if (keys.contains(KeyCode.LEFT))  paddle.move(-6);
        if (keys.contains(KeyCode.RIGHT)) paddle.move(6);
        // cập nhật bóng
        ball.update();
        // va chạm với paddle
        if (ball.intersects(paddle)) ball.bounceVertical();
    }

    public void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);

        for (Brick brick : bricks) {
            brick.draw(gc);
        }
        ball.draw(gc);
        paddle.draw(gc);
    }
}
