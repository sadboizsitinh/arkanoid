package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;

/**
 * Singleton GameManager - manages all game logic and state
 * Implements Singleton pattern for game management
 */
public class GameManager {
    private static GameManager instance;

    // Game objects
    private Paddle paddle;
    private Ball ball;
    private List<Ball> balls; // Multiple balls support
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private List<PowerUp> activePowerUps; // Track active power-ups

    // Deltatime
    private double deltaTime = 0.05;

    // Game state
    private int score;
    private int lives;
    private int level;
    private GameState gameState;
    private boolean[] keys; // For input handling

    // Game dimensions
    private double gameWidth;
    private double gameHeight;

    // Ball speed tracking for power-ups
    private double originalBallSpeed;

    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }

    private GameManager() {
        this.gameWidth = 800;
        this.gameHeight = 600;
        this.keys = new boolean[256];
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.activePowerUps = new ArrayList<>();
        this.balls = new ArrayList<>();
        reset();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startGame() {
        reset();
        gameState = GameState.PLAYING;
    }

    private void reset() {
        score = 0;
        lives = 3;
        level = 1;
        gameState = GameState.MENU;

        // Initialize paddle
        paddle = new Paddle(gameWidth / 2 - 50, gameHeight - 50);

        // Initialize ball
        ball = new Ball(gameWidth / 2 - 10, gameHeight / 2);
        originalBallSpeed = ball.getSpeed();

        // Clear and add main ball to balls list
        balls.clear();
        balls.add(ball);

        // Initialize bricks
        createLevel(level);

        // Clear power-ups
        powerUps.clear();
        activePowerUps.clear();
    }

    /**
     * Create bricks for the current level
     */
    private void createLevel(int level) {
        bricks.clear();

        int rows = 5 + level; // More rows as level increases
        int cols = 10;
        double brickWidth = gameWidth / cols;
        double brickHeight = 25;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = col * brickWidth;
                double y = 50 + row * brickHeight;

                // Create different brick types based on position
                if (row == 0) {
                    bricks.add(new StrongBrick(x, y, brickWidth - 2, brickHeight - 2));
                } else {
                    bricks.add(new NormalBrick(x, y, brickWidth - 2, brickHeight - 2));
                }
            }
        }
    }

    /**
     * Main game update loop
     */
    public void updateGame(double deltaTime) {
        if (gameState != GameState.PLAYING) return;

        handleInput(deltaTime);

        // Update game objects
        paddle.update(deltaTime);

        // Update all balls
        for (Ball b : balls) {
            b.update(deltaTime);
        }

        // Update falling power-ups
        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);
        }

        // Update active power-ups and remove expired ones
        activePowerUps.removeIf(powerUp -> {
            powerUp.update(deltaTime);
            if (powerUp.isExpired()) {
                powerUp.removeEffect(paddle);
                return true;
            }
            return false;
        });

        checkCollisions();
        checkGameConditions();
    }

    private void handleInput(double deltaTime) {
        // This would be called from the main application with current key states
    }

    public void handleKeyPress(String key) {
        keys['A'] = false;
        keys['D'] = false;
        if (gameState == GameState.PLAYING) {
            switch (key.toUpperCase()) {
                case "A":
                case "LEFT":
                    keys['A'] = true;
                    paddle.moveLeft(deltaTime);
                    break;
                case "D":
                case "RIGHT":
                    keys['D'] = true;
                    paddle.moveRight(deltaTime);
                    break;
                case "S":
                    paddle.setDx(0);
                    break;
                case "P":
                    togglePause();
                    break;
                default:
                    break;
            }
        }

        if (keys['A']) {
            paddle.moveLeft(deltaTime);
        }

        if (keys['D']) {
            paddle.moveRight(deltaTime);
        }
    }

    private void togglePause() {
        gameState = (gameState == GameState.PLAYING) ? GameState.PAUSED : GameState.PLAYING;
    }

    /**
     * Check all collision detection
     */
    private void checkCollisions() {
        // Process each ball
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball currentBall = ballIterator.next();

            // Ball-Wall collisions
            if (currentBall.getX() <= 0) {
                currentBall.setX(0);
                currentBall.bounceOffWall('L');
            }
            if (currentBall.getX() + currentBall.getWidth() >= gameWidth) {
                currentBall.setX(gameWidth - currentBall.getWidth());
                currentBall.bounceOffWall('R');
            }
            if (currentBall.getY() <= 0) {
                currentBall.setY(0);
                currentBall.bounceOffWall('T');
            }

            // Ball falls below paddle
            if (currentBall.getY() > gameHeight) {
                ballIterator.remove();

                // Only lose life if all balls are gone
                if (balls.isEmpty()) {
                    lives--;
                    if (lives <= 0) {
                        gameState = GameState.GAME_OVER;
                    } else {
                        resetBallAndPaddle();
                    }
                }
                continue;
            }

            // Ball-Paddle collision
            if (currentBall.intersects(paddle)) {
                currentBall.bounceOffPaddle(paddle);
            }

            // Ball-Brick collisions
            for (Brick brick : bricks) {
                if (currentBall.intersects(brick)) {
                    currentBall.bounceOff(brick);
                    brick.takeHit();

                    if (brick.isDestroyed()) {
                        score += brick.getPoints();

                        // Random chance to spawn power-up
                        if (Math.random() < 0.3) { // 30% chance
                            spawnRandomPowerUp(brick.getX() + brick.getWidth() / 2,
                                    brick.getY() + brick.getHeight());
                        }
                    }
                    break; // Only one collision per frame
                }
            }
        }

        // Remove destroyed bricks after checking all balls
        bricks.removeIf(Brick::isDestroyed);

        // Paddle-PowerUp collisions
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            if (paddle.intersects(powerUp)) {
                applyPowerUpEffect(powerUp);
                powerUpIterator.remove();
            } else if (powerUp.getY() > gameHeight) {
                powerUpIterator.remove(); // Remove if falls off screen
            }
        }
    }

    private void spawnRandomPowerUp(double x, double y) {
        double rand = Math.random();
        PowerUp powerUp;

        // Tỷ lệ đều nhau: 20% cho mỗi loại (5 loại power-up)
        if (rand < 0.2) {
            powerUp = new ExpandPaddlePowerUp(x, y);
        } else if (rand < 0.4) {
            powerUp = new FastBallPowerUp(x, y);
        } else if (rand < 0.6) {
            powerUp = new SlowBallPowerUp(x, y);
        } else if (rand < 0.8) {
            powerUp = new ExtraLifePowerUp(x, y);
        } else {
            powerUp = new MultiBallPowerUp(x, y);
        }

        powerUps.add(powerUp);
    }

    private void applyPowerUpEffect(PowerUp powerUp) {
        switch (powerUp.type) {
            case EXPAND_PADDLE:
                // Remove previous expand effect if exists
                activePowerUps.removeIf(p -> p.type == PowerUp.PowerUpType.EXPAND_PADDLE);
                powerUp.applyEffect(paddle);
                activePowerUps.add(powerUp);
                break;

            case FAST_BALL:
                // Remove any speed effect
                activePowerUps.removeIf(p -> p.type == PowerUp.PowerUpType.FAST_BALL ||
                        p.type == PowerUp.PowerUpType.SLOW_BALL);
                for (Ball b : balls) {
                    b.applySpeed(originalBallSpeed * 1.5);
                }
                powerUp.activate();
                activePowerUps.add(powerUp);
                break;

            case SLOW_BALL:
                // Remove any speed effect
                activePowerUps.removeIf(p -> p.type == PowerUp.PowerUpType.FAST_BALL ||
                        p.type == PowerUp.PowerUpType.SLOW_BALL);
                for (Ball b : balls) {
                    b.applySpeed(originalBallSpeed * 0.7);
                }
                powerUp.activate();
                activePowerUps.add(powerUp);
                break;

            case EXTRA_LIFE:
                lives++;
                powerUp.activate();
                break;

            case MULTI_BALL:
                spawnMultiBalls();
                score += 50;
                break;
        }
    }

    /**
     * Spawn 2 additional balls in different directions
     */
    private void spawnMultiBalls() {
        if (balls.isEmpty()) return;

        Ball originalBall = balls.get(0);

        // Create 2 new balls
        for (int i = 0; i < 2; i++) {
            Ball newBall = new Ball(originalBall.getX(), originalBall.getY());
            newBall.setTypeSkin(originalBall.getTypeSkin());
            newBall.applySpeed(originalBall.getSpeed());

            // Set different directions
            double angle = Math.toRadians(-60 + i * 60); // -60° and 60°
            double dx = Math.sin(angle);
            double dy = -Math.cos(angle);
            newBall.setDirection(dx, dy);

            balls.add(newBall);
        }
    }

    private void checkGameConditions() {
        // Check if all bricks destroyed
        if (bricks.isEmpty()) {
            level++;
            createLevel(level);
            resetBallAndPaddle();
        }
    }

    private void resetBallAndPaddle() {
        balls.clear();

        ball = new Ball(gameWidth / 2 - 10, gameHeight / 2);
        ball.setDirection(1, -1);
        ball.applySpeed(originalBallSpeed);
        balls.add(ball);

        paddle.setX(gameWidth / 2 - paddle.getWidth() / 2);
        paddle.resetSize();

        // Clear all active power-ups
        activePowerUps.clear();
    }

    /**
     * Render all game objects
     */
    public void render(GraphicsContext gc) {
        // Clear screen
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameWidth, gameHeight);

        if (gameState == GameState.MENU) {
            renderMenu(gc);
        } else if (gameState == GameState.GAME_OVER) {
            renderGameOver(gc);
        } else {
            // Render game objects
            paddle.render(gc);

            // Render all balls
            for (Ball b : balls) {
                b.render(gc);
            }

            for (Brick brick : bricks) {
                brick.render(gc);
            }

            for (PowerUp powerUp : powerUps) {
                powerUp.render(gc);
            }

            renderUI(gc);
            renderActivePowerUps(gc);

            if (gameState == GameState.PAUSED) {
                renderPauseOverlay(gc);
            }
        }
    }

    private void renderMenu(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillText("ARKANOID", gameWidth / 2 - 50, gameHeight / 2 - 50);
        gc.fillText("Press SPACE to Start", gameWidth / 2 - 80, gameHeight / 2);
        gc.fillText("Use A/D or Arrow Keys to Move", gameWidth / 2 - 100, gameHeight / 2 + 30);
    }

    private void renderGameOver(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillText("GAME OVER", gameWidth / 2 - 60, gameHeight / 2 - 20);
        gc.setFill(Color.WHITE);
        gc.fillText("Final Score: " + score, gameWidth / 2 - 50, gameHeight / 2 + 20);
        gc.fillText("Press R to Restart", gameWidth / 2 - 60, gameHeight / 2 + 50);
    }

    private void renderUI(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 10, 20);
        gc.fillText("Lives: " + lives, 10, 40);
        gc.fillText("Level: " + level, 10, 60);
        gc.fillText("Balls: " + balls.size(), gameWidth - 80, 20);
    }

    private void renderActivePowerUps(GraphicsContext gc) {
        gc.setFill(Color.YELLOW);
        int yOffset = 80;
        for (PowerUp powerUp : activePowerUps) {
            if (powerUp.timeRemaining > 0) {
                String text = powerUp.getSymbol() + ": " +
                        String.format("%.1f", powerUp.timeRemaining) + "s";
                gc.fillText(text, 10, yOffset);
                yOffset += 20;
            }
        }
    }

    private void renderPauseOverlay(GraphicsContext gc) {
        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.fillRect(0, 0, gameWidth, gameHeight);
        gc.setFill(Color.WHITE);
        gc.fillText("PAUSED", gameWidth / 2 - 30, gameHeight / 2);
        gc.fillText("Press P to Resume", gameWidth / 2 - 60, gameHeight / 2 + 30);
    }

    // Getters for external access
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState state) { this.gameState = state; }
    public double getGameWidth() { return gameWidth; }
    public double getGameHeight() { return gameHeight; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getLevel() { return level; }
}