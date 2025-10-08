package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
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

    // Game state
    private int score;
    private int lives;
    private int level;
    private GameState gameState;

    // Input handling - simple boolean flags
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // Game dimensions
    private double gameWidth;
    private double gameHeight;

    // Ball speed tracking for power-ups
    private static final double DEFAULT_BALL_SPEED = 325.0;
    private double originalBallSpeed;

    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }

    private GameManager() {
        this.gameWidth = 800;
        this.gameHeight = 600;
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.activePowerUps = new ArrayList<>();
        this.balls = new ArrayList<>();
        this.originalBallSpeed = DEFAULT_BALL_SPEED;
        reset();
    }

    public void setMovingLeft(boolean moving) {
        this.movingLeft = moving;
        // ✅ Release ball khi bắt đầu di chuyển
        releaseBallsFromPaddle();
    }

    public void setMovingRight(boolean moving) {
        this.movingRight = moving;
        // ✅ Release ball khi bắt đầu di chuyển
        releaseBallsFromPaddle();
    }

    /**
     * ✅ Release tất cả các ball đang dính trên paddle
     */
    private void releaseBallsFromPaddle() {
        if (gameState == GameState.PLAYING) {
            for (Ball b : balls) {
                if (b.isStuckToPaddle()) {
                    b.release();
                }
            }
        }
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startGame() {
        System.out.println("startGame called, current state: " + gameState);

        reset();
        gameState = GameState.PLAYING;

        System.out.println("Game started, new state: " + gameState);
    }

    private void reset() {
        System.out.println("Resetting game...");

        score = 0;
        lives = 3;
        level = 1;
        movingLeft = false;
        movingRight = false;

        originalBallSpeed = DEFAULT_BALL_SPEED;

        // Initialize paddle
        paddle = new Paddle(gameWidth / 2 - 50, gameHeight - 50);

        // ✅ Initialize ball và dính lên paddle
        ball = new Ball(gameWidth / 2 - 10, gameHeight / 2);
        ball.applySpeed(originalBallSpeed);
        ball.stickToPaddle(paddle); // Dính lên paddle

        // Clear and add main ball to balls list
        balls.clear();
        balls.add(ball);

        // Initialize bricks
        createLevel(level);

        powerUps.clear();

        for (PowerUp powerUp : activePowerUps) {
            powerUp.removeEffect(paddle);
        }
        activePowerUps.clear();

        System.out.println("Reset complete - Score: " + score + ", Lives: " + lives + ", Bricks: " + bricks.size());
    }

    /**
     * Create bricks for the current level from CSV file
     */
    private void createLevel(int level) {
        bricks.clear();

        String filename = "map" + level + ".csv";
        try (Scanner scanner = new Scanner(new java.io.File("maps/" + filename))) {
            int row = 0;
            double brickWidth = gameWidth / 10;
            double brickHeight = 25;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",");
                for (int col = 0; col < values.length; col++) {
                    int type = Integer.parseInt(values[col].trim());
                    double x = col * brickWidth;
                    double y = 50 + row * brickHeight;
                    bricks.add(new StrongBrick(x, y, brickWidth - 2, brickHeight - 2));
                    switch (type) {
                        case 1:
                            bricks.add(new NormalBrick(x, y, brickWidth - 2, brickHeight - 2));
                            break;
                        case 2:
                            bricks.add(new StrongBrick(x, y, brickWidth - 2, brickHeight - 2));
                            break;
                        case 3:
                            bricks.add(new UnbreakableBrick(x, y, brickWidth - 2, brickHeight - 2));
                            break;
                        default:
                            break;
                    }
                }
                row++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            int rows = 5 + level;
            int cols = 10;
            double brickWidth = gameWidth / cols;
            double brickHeight = 25;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    double x = c * brickWidth;
                    double y = 50 + r * brickHeight;
                    bricks.add(new StrongBrick(x, y, brickWidth - 2, brickHeight - 2));
                }
            }
        }
    }


    /**
     * Main game update loop
     */
    public void updateGame(double deltaTime) {
        if (gameState != GameState.PLAYING) return;

        // Handle paddle movement with simple flags
        if (movingLeft) {
            paddle.moveLeft(deltaTime);
        }
        if (movingRight) {
            paddle.moveRight(deltaTime);
        }

        // ✅ Update vị trí ball khi dính trên paddle
        for (Ball b : balls) {
            if (b.isStuckToPaddle()) {
                b.updateStuckPosition(paddle);
            }
        }

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

                if (powerUp.type == PowerUp.PowerUpType.FAST_BALL ||
                        powerUp.type == PowerUp.PowerUpType.SLOW_BALL) {
                    for (Ball b : balls) {
                        b.applySpeed(originalBallSpeed);
                    }
                }
                return true;
            }
            return false;
        });

        checkCollisions();
        checkGameConditions();
    }

    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
        }
    }

    /**
     * Check all collision detection
     */
    private void checkCollisions() {
        // Process each ball
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball currentBall = ballIterator.next();

            // ✅ Bỏ qua collision check nếu ball đang dính
            if (currentBall.isStuckToPaddle()) {
                continue;
            }

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
                powerUpIterator.remove();
            }
        }
    }

    private void spawnRandomPowerUp(double x, double y) {
        double rand = Math.random();
        PowerUp powerUp;

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
                activePowerUps.removeIf(p -> {
                    if (p.type == PowerUp.PowerUpType.EXPAND_PADDLE) {
                        p.removeEffect(paddle);
                        return true;
                    }
                    return false;
                });
                powerUp.applyEffect(paddle);
                activePowerUps.add(powerUp);
                break;

            case FAST_BALL:
                activePowerUps.removeIf(p ->
                        p.type == PowerUp.PowerUpType.FAST_BALL ||
                                p.type == PowerUp.PowerUpType.SLOW_BALL
                );
                for (Ball b : balls) {
                    b.applySpeed(originalBallSpeed * 1.5);
                }
                powerUp.activate();
                activePowerUps.add(powerUp);
                break;

            case SLOW_BALL:
                activePowerUps.removeIf(p ->
                        p.type == PowerUp.PowerUpType.FAST_BALL ||
                                p.type == PowerUp.PowerUpType.SLOW_BALL
                );
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

        // ✅ Chỉ spawn nếu ball không dính trên paddle
        if (originalBall.isStuckToPaddle()) {
            return;
        }

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
            newBall.release(); // Đảm bảo ball mới không dính

            balls.add(newBall);
        }
    }

    private void checkGameConditions() {
        boolean cleared = true;
        for (Brick brick : bricks) {
            if (!brick.isDestroyed() && brick.getType() != Brick.BrickType.UNBREAKABLE) {
                cleared = false;
                break;
            }
        }

        if (cleared) {
            level++;
            createLevel(level);
            resetBallAndPaddle();
        }
    }


    private void resetBallAndPaddle() {
        balls.clear();

        // ✅ Reset ball và dính lên paddle
        ball = new Ball(gameWidth / 2 - 10, gameHeight / 2);
        ball.setDirection(1, -1);
        ball.applySpeed(originalBallSpeed);
        ball.stickToPaddle(paddle); // Dính lên paddle
        balls.add(ball);

        paddle.setX(gameWidth / 2 - paddle.getWidth() / 2);
        paddle.resetSize();

        for (PowerUp powerUp : activePowerUps) {
            powerUp.removeEffect(paddle);
        }
        activePowerUps.clear();
    }

    /**
     * Render all game objects
     */
    public void render(GraphicsContext gc) {
        // Clear screen với gradient background đẹp hơn
        javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.web("#1e293b")),
                new javafx.scene.paint.Stop(1, Color.web("#0f172a"))
        );
        gc.setFill(gradient);
        gc.fillRect(0, 0, gameWidth, gameHeight);

        // Thêm viền trang trí
        gc.setStroke(Color.web("#0ea5e9"));
        gc.setLineWidth(2);
        gc.strokeRect(1, 1, gameWidth - 2, gameHeight - 2);

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

            // ✅ Hiển thị hướng dẫn khi ball đang dính
            if (balls.stream().anyMatch(Ball::isStuckToPaddle)) {
                renderStuckBallHint(gc);
            }
        }
    }

    /**
     * ✅ Hiển thị hint khi ball đang dính trên paddle
     */
    private void renderStuckBallHint(GraphicsContext gc) {
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));
        gc.setFill(Color.web("#fbbf24"));
        String hint = "Move paddle to start!";
        double textWidth = 180; // Ước lượng
        gc.fillText(hint, gameWidth / 2 - textWidth / 2, gameHeight - 100);
    }

    private void renderMenu(GraphicsContext gc) {
        // Title với hiệu ứng đẹp
        gc.setFill(Color.web("#0ea5e9"));
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 64));
        gc.fillText("ARKANOID", gameWidth / 2 - 150, gameHeight / 2 - 80);

        // Shadow cho title
        gc.setFill(Color.web("#0369a1"));
        gc.fillText("ARKANOID", gameWidth / 2 - 148, gameHeight / 2 - 82);

        // Instructions
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 20));
        gc.setFill(Color.WHITE);
        gc.fillText("Press SPACE to Start", gameWidth / 2 - 100, gameHeight / 2);

        gc.setFill(Color.web("#8b93a5"));
        gc.setFont(javafx.scene.text.Font.font("Arial", 16));
        gc.fillText("Use A/D or Arrow Keys to Move", gameWidth / 2 - 120, gameHeight / 2 + 40);
        gc.fillText("Press P to Pause", gameWidth / 2 - 70, gameHeight / 2 + 70);
    }

    private void renderGameOver(GraphicsContext gc) {
        // Game Over text với màu đỏ nổi bật
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 56));
        gc.setFill(Color.web("#ef4444"));
        gc.fillText("GAME OVER", gameWidth / 2 - 130, gameHeight / 2 - 40);

        // Stats box background
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(gameWidth / 2 - 120, gameHeight / 2 - 10, 240, 80, 12, 12);

        // Stats
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 22));
        gc.setFill(Color.WHITE);
        gc.fillText("Final Score: " + score, gameWidth / 2 - 80, gameHeight / 2 + 20);
        gc.fillText("Level: " + level, gameWidth / 2 - 40, gameHeight / 2 + 50);

        // Instruction
        gc.setFont(javafx.scene.text.Font.font("Arial", 18));
        gc.setFill(Color.web("#0ea5e9"));
        gc.fillText("Press R to Restart", gameWidth / 2 - 80, gameHeight / 2 + 100);
    }

    private void renderUI(GraphicsContext gc) {
        // UI panel background
        gc.setFill(Color.web("#1e293b", 0.8));
        gc.fillRoundRect(5, 5, 200, 90, 8, 8);

        // UI text với màu sắc đẹp
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));

        gc.setFill(Color.web("#0ea5e9"));
        gc.fillText("Score: ", 15, 28);
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(score), 85, 28);

        gc.setFill(Color.web("#ec4899"));
        gc.fillText("Lives: ", 15, 52);
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(lives), 85, 52);

        gc.setFill(Color.web("#10b981"));
        gc.fillText("Level: ", 15, 76);
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(level), 85, 76);

        // Balls counter (góc phải)
        if (balls.size() > 1) {
            gc.setFill(Color.web("#1e293b", 0.8));
            gc.fillRoundRect(gameWidth - 100, 5, 90, 35, 8, 8);

            gc.setFill(Color.web("#fbbf24"));
            gc.fillText("Balls: ", gameWidth - 95, 28);
            gc.setFill(Color.WHITE);
            gc.fillText(String.valueOf(balls.size()), gameWidth - 35, 28);
        }
    }

    private void renderActivePowerUps(GraphicsContext gc) {
        if (activePowerUps.isEmpty()) return;

        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 16));
        int yOffset = 110;

        for (PowerUp powerUp : activePowerUps) {
            if (powerUp.timeRemaining > 0) {
                // Background cho power-up item
                gc.setFill(Color.web("#1e293b", 0.8));
                gc.fillRoundRect(5, yOffset - 20, 190, 28, 6, 6);

                // Icon và text
                gc.setFill(Color.web("#fbbf24"));
                gc.fillText(powerUp.getSymbol(), 15, yOffset);

                gc.setFill(Color.WHITE);
                String text = String.format("%.1fs", powerUp.timeRemaining);
                gc.fillText(text, 150, yOffset);

                yOffset += 35;
            }
        }
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