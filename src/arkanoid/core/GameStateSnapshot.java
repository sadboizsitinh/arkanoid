package arkanoid.core;

import arkanoid.entities.*;
import java.io.Serializable;
import java.util.*;

/**
 * Snapshot của game state để lưu/khôi phục
 */
public class GameStateSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    // Game stats
    public int score;
    public int lives;
    public int level;

    // Paddle state
    public double paddleX;
    public double paddleY;
    public double paddleWidth;

    // Balls state
    public List<BallState> ballStates;

    // Bricks state
    public List<BrickState> brickStates;

    // PowerUps state
    public List<PowerUpState> powerUpStates;
    public List<ActivePowerUpState> activePowerUpStates;

    // ✅ Inner classes cũng phải Serializable
    public static class BallState implements Serializable {
        private static final long serialVersionUID = 1L;
        public double x, y;
        public double dx, dy;
        public double speed;
        public boolean stuckToPaddle;
        public int typeSkin;
    }

    public static class BrickState implements Serializable {
        private static final long serialVersionUID = 1L;
        public Brick.BrickType type;
        public double x, y, width, height;
        public int hitPoints;
    }

    public static class PowerUpState implements Serializable {
        private static final long serialVersionUID = 1L;
        public PowerUp.PowerUpType type;
        public double x, y;
    }

    public static class ActivePowerUpState implements Serializable {
        private static final long serialVersionUID = 1L;
        public PowerUp.PowerUpType type;
        public double timeRemaining;
    }

    /**
     * Tạo snapshot từ GameManager
     */
    public static GameStateSnapshot createSnapshot(GameManager gm) {
        GameStateSnapshot snapshot = new GameStateSnapshot();

        // Lưu stats
        snapshot.score = gm.getScore();
        snapshot.lives = gm.getLives();
        snapshot.level = gm.getLevel();

        // Lưu paddle
        Paddle paddle = gm.getPaddle();
        snapshot.paddleX = paddle.getX();
        snapshot.paddleY = paddle.getY();
        snapshot.paddleWidth = paddle.getWidth();

        // Lưu balls
        snapshot.ballStates = new ArrayList<>();
        for (Ball ball : gm.getBalls()) {
            BallState bs = new BallState();
            bs.x = ball.getX();
            bs.y = ball.getY();
            bs.dx = ball.getDx();
            bs.dy = ball.getDy();
            bs.speed = ball.getSpeed();
            bs.stuckToPaddle = ball.isStuckToPaddle();
            bs.typeSkin = ball.getTypeSkin();
            snapshot.ballStates.add(bs);
        }

        // Lưu bricks
        snapshot.brickStates = new ArrayList<>();
        for (Brick brick : gm.getBricks()) {
            if (!brick.isDestroyed()) {
                BrickState brickState = new BrickState();
                brickState.type = brick.getType();
                brickState.x = brick.getX();
                brickState.y = brick.getY();
                brickState.width = brick.getWidth();
                brickState.height = brick.getHeight();
                brickState.hitPoints = brick.getHitPoints();
                snapshot.brickStates.add(brickState);
            }
        }

        // Lưu falling power-ups
        snapshot.powerUpStates = new ArrayList<>();
        for (PowerUp powerUp : gm.getPowerUps()) {
            PowerUpState pState = new PowerUpState();
            pState.type = powerUp.getType();
            pState.x = powerUp.getX();
            pState.y = powerUp.getY();
            snapshot.powerUpStates.add(pState);
        }

        // Lưu active power-ups
        snapshot.activePowerUpStates = new ArrayList<>();
        for (PowerUp powerUp : gm.getActivePowerUps()) {
            ActivePowerUpState apState = new ActivePowerUpState();
            apState.type = powerUp.getType();
            apState.timeRemaining = powerUp.getTimeRemaining();
            snapshot.activePowerUpStates.add(apState);
        }

        return snapshot;
    }
}