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
    private List<Brick> bricks;
    private List<PowerUp> powerUps;


    // Game state
    private int score;
    private int lives;
    private int level;
    private GameState gameState;
    private boolean[] keys; // For input handling

    // Game dimensions
    private double gameWidth;
    private double gameHeight;

    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }

    private GameManager() {
        this.gameWidth = 800;
        this.gameHeight = 600;
        this.keys = new boolean[256];
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
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

        // Initialize bricks
        createLevel(level);

        // Clear power-ups
        powerUps.clear();
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
        ball.update(deltaTime);


        // Update power-ups
        powerUps.removeIf(PowerUp::isExpired);
        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);
        }

        checkCollisions();
        checkGameConditions();
    }

    private void handleInput(double deltaTime) {
        // This would be called from the main application with current key states
        // Simplified for demonstration
    }

    public void handleKeyPress(String key) {
        if (gameState == GameState.PLAYING) {
            switch (key.toUpperCase()) {
                case "A":
                case "LEFT":
                    paddle.moveLeft(0.016); // Approximate deltaTime
                    break;
                case "D":
                case "RIGHT":
                    paddle.moveRight(0.016, gameWidth);
                    break;
                case "P":
                    togglePause();
                    break;
                default:
                    break;
            }

        }
    }

    private void togglePause() {
        gameState = (gameState == GameState.PLAYING) ? GameState.PAUSED : GameState.PLAYING;
    }

    /**
     * Check all collision detection
     */
    private void checkCollisions() {
        // Ball-Wall collisions
        if (ball.getX() <= 0) {
            ball.setX(0);
            ball.bounceOffWall('L');
        }
        if (ball.getX() + ball.getWidth() >= gameWidth) {
            ball.setX(gameWidth - ball.getWidth());
            ball.bounceOffWall('R');
        }
        if (ball.getY() <= 0) {
            ball.setY(0);
            ball.bounceOffWall('T');
        }

        // Ball falls below paddle - lose life
        if (ball.getY() > gameHeight) {
            lives--;
            if (lives <= 0) {
                gameState = GameState.GAME_OVER;
            } else {
                resetBallAndPaddle();
            }
        }

        // Ball-Paddle collision
        if (ball.intersects(paddle)) {
            ball.bounceOffPaddle(paddle);
        }

        // Ball-Brick collisions
        Iterator<Brick> brickIterator = bricks.iterator();
        while (brickIterator.hasNext()) {
            Brick brick = brickIterator.next();
            if (ball.intersects(brick)) {
                ball.bounceOff(brick);
                brick.takeHit();

                if (brick.isDestroyed()) {
                    score += brick.getPoints();
                    brickIterator.remove();

                    // Random chance to spawn power-up
                    if (Math.random() < 0.2) { // 20% chance
                        spawnRandomPowerUp(brick.getX() + brick.getWidth() / 2,
                                brick.getY() + brick.getHeight());
                    }
                }
                break; // Only one collision per frame
            }
        }

        // Paddle-PowerUp collisions
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            if (paddle.intersects(powerUp)) {
                paddle.applyPowerUp(powerUp);
                powerUpIterator.remove();
            } else if (powerUp.getY() > gameHeight) {
                powerUpIterator.remove(); // Remove if falls off screen
            }
        }
    }

    private void spawnRandomPowerUp(double x, double y) {
        if (Math.random() < 0.5) {
            powerUps.add(new ExpandPaddlePowerUp(x, y));
        } else {
            powerUps.add(new FastBallPowerUp(x, y));
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
        ball.setX(gameWidth / 2 - 10);
        ball.setY(gameHeight / 2);
        ball.setDirection(1, -1);

        paddle.setX(gameWidth / 2 - paddle.getWidth() / 2);
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
            ball.render(gc);

            for (Brick brick : bricks) {
                brick.render(gc);
            }

            for (PowerUp powerUp : powerUps) {
                powerUp.render(gc);
            }

            renderUI(gc);

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
