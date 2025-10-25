package arkanoid.core;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import java.util.*;
import arkanoid.entities.*;
import arkanoid.utils.SoundManager;

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
    private List<Integer> availableMaps = new ArrayList<>();

    // Streak
    private int Streak = 0;
    private boolean Collison = false;

    // Camera Shake
    private CameraShake cameraShake;

    // Game state
    private int score;
    private int lives;
    private int level;
    private int difficultyLevel = 1;
    private GameState gameState;
    private GameStateSnapshot savedSnapshot = null;

    // Input handling - simple boolean flags
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // Game dimensions
    private double gameWidth;
    private double gameHeight;

    private Image cachedBackground = null;
    private boolean backgroundLoaded = false;

    // Ball speed tracking for power-ups
    private static final double DEFAULT_BALL_SPEED = 325.0;
    private double originalBallSpeed;

    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }


    public GameManager() {
        this.gameWidth = 800;
        this.gameHeight = 600;
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.activePowerUps = new ArrayList<>();
        this.balls = new ArrayList<>();
        this.originalBallSpeed = DEFAULT_BALL_SPEED;
        this.cameraShake = new CameraShake(); // ✅ Khởi tạo camera shake
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
        difficultyLevel = 1;
        movingLeft = false;
        movingRight = false;

        availableMaps.clear();
        for (int i = 1; i <= 9; i++) {
            availableMaps.add(i);
        }

        originalBallSpeed = DEFAULT_BALL_SPEED;

        // Initialize paddle
        paddle = new Paddle(gameWidth / 2 - 50, gameHeight - 80);

        // Initialize ball và dính lên paddle
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
        String filename;

        // Nếu là màn chia hết cho 10 → luôn là map10 (đặc biệt)
        if (level % 10 == 0) {
            filename = "map10.csv";
        }
        // Nếu là từ 1 → 9 → dùng map tương ứng
        else if (level <= 9) {
            filename = "map" + level + ".csv";
        }
        // Các màn còn lại (11–19, 21–29, ...)
        else {
            // Khi hết map 1–9 reset danh sách và tăng độ khó
            if (availableMaps.isEmpty()) {
                for (int i = 1; i <= 9; i++) availableMaps.add(i);
                difficultyLevel++;
            }

            // Chọn ngẫu nhiên 1 map chưa dùng
            int index = (int) (Math.random() * availableMaps.size());
            int mapNumber = availableMaps.remove(index);

            filename = "map" + mapNumber + ".csv";
        }

        try {
            // 1) Ưu tiên ../assets/maps/<filename>
            java.nio.file.Path file = java.nio.file.Paths.get("..", "assets", "maps", filename).normalize();

            // 2) Nếu không có, fallback về vị trí hiện tại của bạn
            if (!java.nio.file.Files.isRegularFile(file)) {
                file = java.nio.file.Paths.get("src","arkanoid","assets","maps", filename).normalize();
                if (!java.nio.file.Files.isRegularFile(file)) {
                    file = java.nio.file.Paths.get("..","src","arkanoid","assets","maps", filename).normalize();
                }
            }

            try (java.util.Scanner scanner = new java.util.Scanner(
                    java.nio.file.Files.newBufferedReader(file, java.nio.charset.StandardCharsets.UTF_8))) {

                int row = 0;
                double brickWidth = gameWidth / 10.0, brickHeight = 25.0;

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] values = line.split(",", -1); // giữ cột trống
                    for (int col = 0; col < values.length; col++) {
                        String raw = values[col].trim();
                        if (raw.isEmpty()) continue;
                        int type; try { type = Integer.parseInt(raw); } catch (NumberFormatException ex) { continue; }
                        if (type == 0) continue;

                        double x = col * brickWidth, y = 50 + row * brickHeight;
                        switch (type) {
                            case 1 -> bricks.add(new NormalBrick(x, y, brickWidth - 2, brickHeight - 2));
                            case 2 -> bricks.add(new StrongBrick(x, y, brickWidth - 2, brickHeight - 2));
                            case 3 -> bricks.add(new UnbreakableBrick(x, y, brickWidth - 2, brickHeight - 2));
                        }
                    }
                    row++;
                }
            }
// ⚡ Tăng tốc độ bóng theo độ khó
            originalBallSpeed = DEFAULT_BALL_SPEED * (1 + 0.1 * (difficultyLevel - 1));
            ball.applySpeed(originalBallSpeed);

            System.out.println(" Loaded " + filename + " | Difficulty: " + difficultyLevel);

        } catch (Exception e) {
            System.err.println("Cannot load " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Main game update loop
     */
    public void updateGame(double deltaTime) {
        if (gameState != GameState.PLAYING) return;

        // ✅ Cập nhật camera shake
        cameraShake.update(deltaTime);

        paddle.update(deltaTime);

        // Handle paddle movement with simple flags
        if (movingLeft) {
            paddle.moveLeft(deltaTime);
        }
        if (movingRight) {
            paddle.moveRight(deltaTime);
        }

        // Update vị trí ball khi dính trên paddle
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

                if (powerUp.getType() == PowerUp.PowerUpType.FAST_BALL ||
                        powerUp.getType() == PowerUp.PowerUpType.SLOW_BALL) {
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
        boolean inMoment = false;
        while (ballIterator.hasNext()) {
            Ball currentBall = ballIterator.next();

            // Bỏ qua collision check nếu ball đang dính
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
                        SoundManager.play("game_over.wav");
                        gameState = GameState.GAME_OVER;
                    } else {
                        SoundManager.play("matmang.wav");
                        resetBallAndPaddle();
                    }
                }
                continue;
            }

            // Ball-Paddle collision
            if (currentBall.intersects(paddle)) {
                currentBall.bounceOffPaddle(paddle);
                paddle.triggerHitAnimation();
                cameraShake.shakeOnPaddleHit();
                SoundManager.play("paddle.wav");
                Collison = false;
            }

            // Ball-Brick collisions
            for (Brick brick : bricks) {
                if (currentBall.intersects(brick)) {
                    currentBall.bounceOff(brick);
                    brick.takeHit();
                    Collison = true;
                    inMoment = true;

                    // ✅ Shake mạnh khi brick bị vỡ
                    if (brick.isDestroyed()) {
                        cameraShake.shakeOnBrickHit(); // Shake mạnh
                        SoundManager.play("gachvo.wav");
                        score += brick.getPoints();

                        // Random chance to spawn power-up
                        if (Math.random() < 0.3) { // 30% chance
                            spawnRandomPowerUp(brick.getX() + brick.getWidth() / 2,
                                    brick.getY() + brick.getHeight());
                        }
                    } else {
                        // ✅ Shake nhẹ khi brick bị hit nhưng chưa vỡ
                        cameraShake.shakeOnPaddleHit();
                        SoundManager.play("gach.wav");
                    }
                    break; // Only one collision per frame
                }
            }
        }

        if (inMoment) {
            if (Collison == false) {
                Streak = 0;
            } else {
                Streak++;
                System.out.println("Streak: " + Streak);
                if (Streak >= 4 && Streak % 4 == 0) {
                    // System.exit(0);
                    score += 50 * (Streak / 4);
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
                SoundManager.play("powerup.wav");
                powerUpIterator.remove();
            } else if (powerUp.getY() > gameHeight) {
                powerUpIterator.remove();
            }
        }
    }

    private void spawnRandomPowerUp(double x, double y) {
        double rand = Math.random();
        PowerUp powerUp;

        if (rand < 0.3) {
            powerUp = new ExpandPaddlePowerUp(x, y);
        } else if (rand < 0.55) {
            powerUp = new FastBallPowerUp(x, y);
        } else if (rand < 0.8) {
            powerUp = new SlowBallPowerUp(x, y);
        } else if (rand < 0.95) {
            powerUp = new MultiBallPowerUp(x, y);
        } else {
            powerUp = new ExtraLifePowerUp(x, y);
        }

        powerUps.add(powerUp);
    }

    private void applyPowerUpEffect(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case EXPAND_PADDLE:
                activePowerUps.removeIf(p -> {
                    if (p.getType() == PowerUp.PowerUpType.EXPAND_PADDLE) {
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
                        p.getType() == PowerUp.PowerUpType.FAST_BALL ||
                                p.getType() == PowerUp.PowerUpType.SLOW_BALL
                );
                for (Ball b : balls) {
                    b.applySpeed(originalBallSpeed * 1.5);
                }
                powerUp.activate();
                activePowerUps.add(powerUp);
                break;

            case SLOW_BALL:
                activePowerUps.removeIf(p ->
                        p.getType() == PowerUp.PowerUpType.FAST_BALL ||
                                p.getType() == PowerUp.PowerUpType.SLOW_BALL
                );
                for (Ball b : balls) {
                    b.applySpeed(originalBallSpeed * 0.7);
                }
                powerUp.activate();
                activePowerUps.add(powerUp);
                break;

            case EXTRA_LIFE:
                if(lives < 3)
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

        // Chỉ spawn nếu ball không dính trên paddle
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
            SoundManager.play("Qua_man.wav");
            level++;
            createLevel(level);
            resetBallAndPaddle();
        }
    }


    private void resetBallAndPaddle() {
        balls.clear();

        // Reset ball và dính lên paddle
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

    private void loadBackgroundImage() {
        if (backgroundLoaded) return;

        try {
            String resourcePath = "/assets/images/bg-retrospace(1).png";
            var resourceStream = getClass().getResourceAsStream(resourcePath);

            if (resourceStream != null) {
                cachedBackground = new Image(resourceStream);
                System.out.println("✅ Background cached from resources");
            } else {
                // Fallback: load từ file
                java.io.File imageFile = new java.io.File("src/arkanoid/assets/images/bg-retrospace(1).png");
                if (imageFile.exists()) {
                    cachedBackground = new Image(imageFile.toURI().toString());
                    System.out.println("✅ Background cached from file");
                } else {
                    System.err.println("⚠️ Background image not found");
                    cachedBackground = null;
                }
            }

            backgroundLoaded = true;

        } catch (Exception e) {
            System.err.println("❌ Error loading background: " + e.getMessage());
            cachedBackground = null;
            backgroundLoaded = true;
        }
    }


    /**
     * Render all game objects
     */
    public void render(GraphicsContext gc) {
        // ✅ Load background chỉ 1 lần
        if (!backgroundLoaded) {
            loadBackgroundImage();
        }

        // ✅ Lấy offset từ camera shake
        double shakeX = cameraShake.getShakeX();
        double shakeY = cameraShake.getShakeY();

        // Lưu trạng thái canvas
        gc.save();

        // ✅ Áp dụng offset rung lên toàn bộ canvas
        gc.translate(shakeX, shakeY);

        // ✅ VẼ BACKGROUND (đã được cache - NHANH!)
        if (cachedBackground != null) {
            gc.drawImage(cachedBackground, -shakeX, -shakeY, gameWidth, gameHeight);
        } else {
            // Fallback: clear với màu đơn sắc nếu không có background
            gc.setFill(Color.web("#0f172a"));
            gc.fillRect(-shakeX, -shakeY, gameWidth, gameHeight);
        }

        // Thêm viền trang trí
        gc.setStroke(Color.web("#0ea5e9"));
        gc.setLineWidth(2);
        gc.strokeRect(1, 1, gameWidth - 2, gameHeight - 2);

        if (gameState == GameState.MENU) {
            renderMenu(gc);
        } else if (gameState == GameState.GAME_OVER) {
            // renderUI(gc);
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

            // Hiển thị hướng dẫn khi ball đang dính
            if (balls.stream().anyMatch(Ball::isStuckToPaddle)) {
                renderStuckBallHint(gc);
            }
        }

        // ✅ Khôi phục trạng thái canvas
        gc.restore();
    }

    // Getter để Ball có thể gọi shake nếu cần
    public CameraShake getCameraShake() {
        return cameraShake;
    }

    /**
     * Hiển thị hint khi ball đang dính trên paddle
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

    private void renderActivePowerUps(GraphicsContext gc) {
        if (activePowerUps.isEmpty()) return;

        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 16));
        int yOffset = 110;

        for (PowerUp powerUp : activePowerUps) {
            if (powerUp.getTimeRemaining() > 0) {
                // Background cho power-up item
                gc.setFill(Color.web("#1e293b", 0.8));
                gc.fillRoundRect(5, yOffset - 20, 190, 28, 6, 6);

                // Icon và text
                gc.setFill(Color.web("#fbbf24"));
                gc.fillText(powerUp.getDisplaySymbol(), 15, yOffset);

                gc.setFill(Color.WHITE);
                String text = String.format("%.1fs", powerUp.getTimeRemaining());
                gc.fillText(text, 150, yOffset);

                yOffset += 35;
            }
        }
    }

    public boolean hasSavedGame() {
        return GameStatePersistence.hasSaveFile();
    }

    /**
     * Lưu snapshot khi pause và về menu
     */
    public void saveGameState() {
        // ✅ Cho phép lưu cả khi PLAYING và PAUSED
        if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            GameStateSnapshot snapshot = GameStateSnapshot.createSnapshot(this);
            GameStatePersistence.saveToFile(snapshot);
            System.out.println("✅ Game state saved to file!");
        } else {
            System.out.println("⚠️ Cannot save game in state: " + gameState);
        }
    }

    /**
     * Xóa snapshot (khi game over hoặc win)
     */
    public void clearSavedGame() {
        GameStatePersistence.deleteSaveFile();
        System.out.println("🗑️ Saved game file deleted");
    }

    /**
     * Continue game từ snapshot đã lưu
     */
    public void continueGame() {
        GameStateSnapshot snapshot = GameStatePersistence.loadFromFile();

        if (snapshot == null) {
            System.err.println("❌ No saved game file to continue!");
            return;
        }

        restoreFromSnapshot(snapshot);
        gameState = GameState.PLAYING;

        System.out.println("▶️ Game continued from file");
        System.out.println("   Score: " + score + ", Lives: " + lives + ", Level: " + level);
    }

    /**
     * Khôi phục game state từ snapshot
     */
    private void restoreFromSnapshot(GameStateSnapshot snapshot) {
        // Khôi phục stats
        this.score = snapshot.score;
        this.lives = snapshot.lives;
        this.level = snapshot.level;

        // Khôi phục paddle
        paddle = new Paddle(snapshot.paddleX, snapshot.paddleY);
        paddle.setWidth(snapshot.paddleWidth);

        // Khôi phục balls
        balls.clear();
        for (GameStateSnapshot.BallState ballState : snapshot.ballStates) {
            Ball ball = new Ball(ballState.x, ballState.y);
            ball.setTypeSkin(ballState.typeSkin);
            ball.applySpeed(ballState.speed);
            ball.setDirection(ballState.dx / ballState.speed, ballState.dy / ballState.speed);

            if (ballState.stuckToPaddle) {
                ball.stickToPaddle(paddle);
            } else {
                ball.release();
            }

            balls.add(ball);
        }

        // Set main ball reference
        if (!balls.isEmpty()) {
            ball = balls.get(0);
        }

        // Khôi phục bricks
        bricks.clear();
        for (GameStateSnapshot.BrickState brickState : snapshot.brickStates) {
            Brick brick = null;
            switch (brickState.type) {
                case NORMAL:
                    brick = new NormalBrick(brickState.x, brickState.y, brickState.width, brickState.height);
                    break;
                case STRONG:
                    brick = new StrongBrick(brickState.x, brickState.y, brickState.width, brickState.height);
                    break;
                case UNBREAKABLE:
                    brick = new UnbreakableBrick(brickState.x, brickState.y, brickState.width, brickState.height);
                    break;
            }

            if (brick != null) {
                // Restore hitPoints
                brick.setHitPoints(brickState.hitPoints);
                brick.updateColor();
                bricks.add(brick);
            }
        }

        // Khôi phục falling powerups
        powerUps.clear();
        for (GameStateSnapshot.PowerUpState pState : snapshot.powerUpStates) {
            PowerUp powerUp = createPowerUpByType(pState.type, pState.x, pState.y);
            if (powerUp != null) {
                powerUps.add(powerUp);
            }
        }

        // Khôi phục active powerups
        activePowerUps.clear();
        for (GameStateSnapshot.ActivePowerUpState apState : snapshot.activePowerUpStates) {
            PowerUp powerUp = createPowerUpByType(apState.type, 0, 0);
            if (powerUp != null) {
                powerUp.activate();
                powerUp.setTimeRemaining(apState.timeRemaining);
                powerUp.applyEffect(paddle);
                activePowerUps.add(powerUp);
            }
        }

        // Reset input flags
        movingLeft = false;
        movingRight = false;
    }

    /**
     * Helper method để tạo PowerUp theo type
     */
    private PowerUp createPowerUpByType(PowerUp.PowerUpType type, double x, double y) {
        switch (type) {
            case EXPAND_PADDLE:
                return new ExpandPaddlePowerUp(x, y);
            case FAST_BALL:
                return new FastBallPowerUp(x, y);
            case SLOW_BALL:
                return new SlowBallPowerUp(x, y);
            case EXTRA_LIFE:
                return new ExtraLifePowerUp(x, y);
            case MULTI_BALL:
                return new MultiBallPowerUp(x, y);
            default:
                return null;
        }
    }

    // Cập nhật phương thức checkGameConditions để clear saved game khi thắng/thua
    // Thêm vào cuối phương thức checkGameConditions():
    /*
    if (cleared) {
        level++;
        createLevel(level);
        resetBallAndPaddle();
        // Clear saved game vì đã qua level mới
        clearSavedGame();
    }
    */

    // Cập nhật startGame để clear saved game khi start mới
    // Thêm vào đầu startGame():
    /*
    public void startGame() {
        System.out.println("startGame called, current state: " + gameState);

        clearSavedGame(); // ✅ Clear saved game khi start mới
        reset();
        gameState = GameState.PLAYING;

        System.out.println("Game started, new state: " + gameState);
    }
    */

    // Getters for external access
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState state) { this.gameState = state; }
    public double getGameWidth() { return gameWidth; }
    public double getGameHeight() { return gameHeight; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getLevel() { return level; }
    //
    public Paddle getPaddle() { return paddle; }
    public List<Ball> getBalls() { return balls; }
    public List<Brick> getBricks() { return bricks; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public List<PowerUp> getActivePowerUps() { return activePowerUps; }
}