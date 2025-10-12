package arkanoid;

import java.util.ArrayList;
import java.util.List;

/**
 * Lưu trữ snapshot của game state để có thể continue sau này
 */
public class GameStateSnapshot {
    // Game stats
    public int score;
    public int lives;
    public int level;

    // Paddle state
    public double paddleX;
    public double paddleY;
    public double paddleWidth;

    // Balls state
    public static class BallState {
        public double x, y;
        public double dx, dy;
        public double speed;
        public int typeSkin;
        public boolean stuckToPaddle;
        public double offsetFromPaddleCenter;

        public BallState(Ball ball) {
            this.x = ball.getX();
            this.y = ball.getY();
            this.dx = ball.getDX();
            this.dy = ball.getDY();
            this.speed = ball.getSpeed();
            this.typeSkin = ball.getTypeSkin();
            this.stuckToPaddle = ball.isStuckToPaddle();
        }
    }
    public List<BallState> ballStates = new ArrayList<>();

    // Bricks state
    public static class BrickState {
        public double x, y, width, height;
        public int hitPoints;
        public Brick.BrickType type;

        public BrickState(Brick brick) {
            this.x = brick.getX();
            this.y = brick.getY();
            this.width = brick.getWidth();
            this.height = brick.getHeight();
            this.hitPoints = brick.hitPoints;
            this.type = brick.getType();
        }
    }
    public List<BrickState> brickStates = new ArrayList<>();

    // PowerUps state (falling)
    public static class PowerUpState {
        public double x, y;
        public PowerUp.PowerUpType type;

        public PowerUpState(PowerUp powerUp) {
            this.x = powerUp.getX();
            this.y = powerUp.getY();
            this.type = powerUp.type;
        }
    }
    public List<PowerUpState> powerUpStates = new ArrayList<>();

    // Active PowerUps state
    public static class ActivePowerUpState {
        public PowerUp.PowerUpType type;
        public double timeRemaining;

        public ActivePowerUpState(PowerUp powerUp) {
            this.type = powerUp.type;
            this.timeRemaining = powerUp.timeRemaining;
        }
    }
    public List<ActivePowerUpState> activePowerUpStates = new ArrayList<>();

    public GameStateSnapshot() {
        // Constructor rỗng
    }

    /**
     * Tạo snapshot từ GameManager hiện tại
     */
    public static GameStateSnapshot createSnapshot(GameManager gm) {
        GameStateSnapshot snapshot = new GameStateSnapshot();

        // Lưu game stats
        snapshot.score = gm.getScore();
        snapshot.lives = gm.getLives();
        snapshot.level = gm.getLevel();

        // Lưu paddle state
        Paddle paddle = gm.getPaddle();
        snapshot.paddleX = paddle.getX();
        snapshot.paddleY = paddle.getY();
        snapshot.paddleWidth = paddle.getWidth();

        // Lưu balls state
        for (Ball ball : gm.getBalls()) {
            snapshot.ballStates.add(new BallState(ball));
        }

        // Lưu bricks state
        for (Brick brick : gm.getBricks()) {
            snapshot.brickStates.add(new BrickState(brick));
        }

        // Lưu falling powerups state
        for (PowerUp powerUp : gm.getPowerUps()) {
            snapshot.powerUpStates.add(new PowerUpState(powerUp));
        }

        // Lưu active powerups state
        for (PowerUp powerUp : gm.getActivePowerUps()) {
            snapshot.activePowerUpStates.add(new ActivePowerUpState(powerUp));
        }

        return snapshot;
    }
}