package arkanoid.core;


import arkanoid.entities.Ball.Ball;
import arkanoid.entities.Brick.Brick;
import arkanoid.entities.Brick.NormalBrick;
import arkanoid.entities.Brick.StrongBrick;
import arkanoid.entities.Brick.UnbreakableBrick;
import arkanoid.entities.Paddle.Paddle;
import arkanoid.entities.PowerUp.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.*;

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

    // Continue
    private boolean isCountdownActive = false;
    private double countdownTime = 0;
    private boolean isCountdownFromMenu = false;

    // Streak
    private int Streak = 0;
    private boolean Collison = false;
    private int lastStreakBonus = 0;
    private boolean excellentEffectActive = false;
    private double excellentEffectTimer = 0.0;
    private int excellentTriggerStreak = 0;

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

    private Map<Ball, Long> lastPaddleHitTime = new HashMap<>();
    private static final long PADDLE_HIT_COOLDOWN = 100; // milliseconds

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

    public void startContinueCountdown(double seconds) {
        isCountdownActive = true;
        countdownTime = seconds;
        isCountdownFromMenu = false; // Continue từ pause
        for (Ball b : balls) {
            b.stickToPaddle(paddle);
        }
    }

    public void startCountdownFromMenu(double seconds) {
        isCountdownActive = true;
        countdownTime = seconds;
        isCountdownFromMenu = true; // Continue từ menu
        for (Ball b : balls) {
            b.stickToPaddle(paddle);
        }
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
        if (isCountdownActive) {
            countdownTime -= deltaTime;
            if (countdownTime <= 0) {
                isCountdownActive = false;
                for (Ball b : balls) {
                    b.release(); // Cho bóng bay lại
                }
            }
            return; // Dừng toàn bộ logic trong khi đang đếm ngược
        }

        if (gameState != GameState.PLAYING) return;

        // ✅ Cập nhật camera shake
        cameraShake.update(deltaTime);

        if (excellentEffectActive) {
            excellentEffectTimer += deltaTime;
            if (excellentEffectTimer > 2.0) { // Hiệu ứng kéo dài 2 giây
                excellentEffectActive = false;
            }
        }

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
        boolean paddleHitThisFrame = false;
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
                lastPaddleHitTime.remove(currentBall);
                Collison = false;
                Streak = 0;
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
                // ✅ CHECK COOLDOWN để tránh double hit
                long currentTime = System.currentTimeMillis();
                Long lastHit = lastPaddleHitTime.get(currentBall);

                if (currentBall.getDY() > 0 &&
                        (lastHit == null || currentTime - lastHit > PADDLE_HIT_COOLDOWN)) {

                    currentBall.bounceOffPaddle(paddle);
                    paddle.triggerHitAnimation();
                    cameraShake.shakeOnPaddleHit();
                    Collison = false;

                    System.out.println("have been");

                    // ✅ Lưu thời gian hit
                    lastPaddleHitTime.put(currentBall, currentTime);

                    if (!paddleHitThisFrame) {
                        SoundManager.play("paddle.wav");
                        paddleHitThisFrame = true;
                    }
                }
            }

            // Ball-Brick collisions
            for (Brick brick : bricks) {
                if (currentBall.intersects(brick)) {
                    currentBall.bounceOff(brick);
                    brick.takeHit();

                    if (brick.getType() != Brick.BrickType.UNBREAKABLE) {
                        Collison = true;
                        inMoment = true;
                    }

                    if (brick.isDestroyed()) {
                        cameraShake.shakeOnBrickHit();
                        SoundManager.play("gachvo.wav");
                        score += brick.getPoints();

                        if (Math.random() < 0.3) {
                            spawnRandomPowerUp(brick.getX() + brick.getWidth() / 2,
                                    brick.getY() + brick.getHeight());
                        }
                    } else {
                        cameraShake.shakeOnPaddleHit();
                        SoundManager.play("gach.wav");
                    }
                    break;
                }
            }
        }

        if (inMoment) {
            if (Collison == false) {
                Streak = 0;
                lastStreakBonus = 0;
                System.out.println("have been");
            } else {
                Streak++;
                System.out.println("Streak: " + Streak);
                if (Streak >= 4) {
                    int streakBonus = 5 * (Streak / 4);
                    lastStreakBonus = streakBonus;
                    addScoreWithAnimation(streakBonus);

                    // ✅ TRIGGER EXCELLENT khi Streak đạt 12 (lần đầu tiên)
                    if (Streak % 2 == 0 && !excellentEffectActive) {
                        excellentEffectActive = true;
                        excellentEffectTimer = 0.0;
                        excellentTriggerStreak = Streak;
                        System.out.println("🌟 EXCELLENT TRIGGERED! Streak: " + Streak);


                        SoundManager.play("streak.wav");
                    }
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

    /**
     * ✅ THÊM MỚI - Cộng điểm với animation
     */
    private void addScoreWithAnimation(int points) {
        if (points <= 0) return;

        int oldScore = score;
        score += points;

        boolean isStreak = (points == lastStreakBonus && lastStreakBonus > 0);
        String emoji = isStreak ? "🔥" : "💚";
        System.out.println(emoji + " Score: " + oldScore + " -> " + score + " (+" + points + ")");
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

        if (excellentEffectActive) {
            renderExcellentEffect(gc);
        }

        // ✅ RENDER COUNTDOWN ĐẸP HƠN
        if (isCountdownActive) {
            renderCountdown(gc);
        }

    }

    private void renderExcellentEffect(GraphicsContext gc) {
        // Tính toán fade và slide
        double progress = excellentEffectTimer / 2.0; // 0 -> 1

        // ✅ PHASE 1: Fade in + Slide in (0 - 0.6s)
        // PHASE 2: Hold (0.6 - 1.2s)
        // PHASE 3: Fade out + Slide out (1.2 - 2.0s)

        double alpha = 0.0;
        double slideX = 0.0;

        if (excellentEffectTimer < 0.6) {
            // Fade in + slide từ phải vào
            double phaseProgress = excellentEffectTimer / 0.6;
            alpha = phaseProgress; // 0 -> 1
            slideX = (1 - phaseProgress) * 100; // 100 -> 0 (slide từ phải)
        } else if (excellentEffectTimer < 1.2) {
            // Giữ sáng + ở chổi
            alpha = 1.0;
            slideX = 0.0;
        } else {
            // Fade out + slide ra phải
            double phaseProgress = (excellentEffectTimer - 1.2) / 0.8;
            alpha = 1.0 - phaseProgress; // 1 -> 0
            slideX = phaseProgress * 100; // 0 -> 100 (slide ra phải)
        }

        // ✅ VỊ TRÍ: Bên phải, chiều cao 40% từ dưới lên
        double canvasHeight = gc.getCanvas().getHeight();
        double canvasWidth = gc.getCanvas().getWidth();

        double boxWidth = 140;    // Kích thước box
        double boxHeight = 50;    // Kích thước box

        // ===== CHỈNH VỊ TRÍ - TÍNH THEO TỈ LỆ =====
        double rightMarginPercent = 0.25;   // Khoảng cách từ mép phải (5% chiều rộng)
        // 0.02 = 2%, 0.05 = 5%, 0.1 = 10%
        double heightPercent = 0.6;         // Chiều cao từ dưới lên (40%)
        // 0.3 = 30%, 0.4 = 40%, 0.5 = 50%

        double x = canvasWidth * (1 - rightMarginPercent) + slideX;  // Bên phải
        double y = canvasHeight * heightPercent;                     // % từ dưới lên

        gc.save();

        // ===== BACKGROUND BOX =====
        gc.setFill(Color.web("#000000", alpha * 0.3));
        gc.fillRoundRect(x - boxWidth/2, y - boxHeight/2, boxWidth, boxHeight, 10, 10);

        // ===== VIỀN VÀNG ĐẸP HƠN - GLOW MULTIPLE LAYERS =====
        // Viền ngoài sáng (mờ hơn)
        gc.setStroke(Color.web("#fbbf24", alpha * 0.7));
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(x - boxWidth/2, y - boxHeight/2, boxWidth, boxHeight, 10, 10);

        // Viền trong
        gc.setStroke(Color.web("#f59e0b", alpha * 0.6));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x - boxWidth/2 + 1, y - boxHeight/2 + 1, boxWidth - 2, boxHeight - 2, 9, 9);

        // ===== GLOW EFFECT (mờ hơn) =====
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web("#fbbf24", alpha * 0.6),
                15,
                0.7,
                0, 0
        ));

        // ===== TEXT "EXCELLENT" =====
        gc.setFont(javafx.scene.text.Font.font("Arial Black",
                javafx.scene.text.FontWeight.BOLD, 24));

        // Shadow đen
        gc.setFill(Color.web("#000000", alpha * 0.5));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText("EXCELLENT!", x + 1, y + 2);

        // Text chính (vàng sáng - mờ hơn)
        gc.setFill(Color.web("#fbbf24", alpha * 0.9));
        gc.fillText("EXCELLENT!", x, y);

        // Inner glow (trắng - mờ hơn)
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web("#ffffff", alpha * 0.5),
                8,
                0.8,
                0, 0
        ));
        gc.setFill(Color.web("#ffffff", alpha * 0.3));
        gc.fillText("EXCELLENT!", x, y);

        // ===== STREAK INFO (dưới EXCELLENT) - FONT RỰC CHÁY (mờ hơn) =====
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web("#ff6b00", alpha * 0.7),
                12,
                0.7,
                0, 0
        ));
        gc.setFont(javafx.scene.text.Font.font("Arial Black",
                javafx.scene.text.FontWeight.BOLD, 16));

        // Shadow cam đậm
        gc.setFill(Color.web("#cc3300", alpha * 0.6));
        gc.fillText("x" + excellentTriggerStreak + " Streak", x + 1, y + 23);

        // Text chính cam rực (mờ hơn)
        gc.setFill(Color.web("#ff6b00", alpha * 0.85));
        gc.fillText("x" + excellentTriggerStreak + " Streak", x, y + 22);

        // Inner glow cam sáng (mờ hơn)
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web("#ffaa33", alpha * 0.6),
                7,
                0.8,
                0, 0
        ));
        gc.setFill(Color.web("#ffaa33", alpha * 0.5));
        gc.fillText("x" + excellentTriggerStreak + " Streak", x, y + 22);


        // ===== SHINE EFFECT (ánh sáng trượt) =====
        if (excellentEffectTimer < 1.2) {
            double shineProgress = excellentEffectTimer / 1.2;
            double shineX = x - boxWidth/2 + shineProgress * boxWidth;

            gc.setEffect(null);

            // Shine gradient
            javafx.scene.paint.LinearGradient shineGradient = new javafx.scene.paint.LinearGradient(
                    shineX - 25, y - boxHeight/2,
                    shineX + 25, y - boxHeight/2,
                    false,
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, Color.web("#ffffff", 0.0)),
                    new javafx.scene.paint.Stop(0.5, Color.web("#ffffff", alpha * 0.6)),
                    new javafx.scene.paint.Stop(1, Color.web("#ffffff", 0.0))
            );

            gc.setFill(shineGradient);
            gc.fillRoundRect(shineX - 25, y - boxHeight/2, 50, boxHeight, 10, 10);
        }

        gc.restore();
    }


    /**
     * ✅ Render countdown đẹp với galaxy theme - CHỈ Ở RIGHT PANEL (800x600)
     * Thêm vào class GameManager.java, thay thế method renderCountdown() cũ
     */
    private void renderCountdown(GraphicsContext gc) {
        int seconds = (int) Math.ceil(countdownTime);

        // ✅ OVERLAY CHỈ CHE RIGHT PANEL (800x600 canvas)
        gc.setFill(Color.web("#000000", 0.75));
        gc.fillRect(0, 0, gameWidth, gameHeight);

        // Tính toán fade và scale cho hiệu ứng mượt
        double fadeProgress = countdownTime - Math.floor(countdownTime);
        double scale = 0.7 + (1.0 - fadeProgress) * 0.5; // Scale từ 0.7 -> 1.2
        double opacity = 0.2 + fadeProgress * 0.8; // Opacity từ 0.2 -> 1.0

        // ✅ MÀU SẮC GALAXY theo số đếm
        String numberColor, glowColor, ringColor;
        switch (seconds) {
            case 3:
                numberColor = "#a855f7";  // Tím galaxy
                glowColor = "#c084fc";    // Tím sáng
                ringColor = "#7c3aed";    // Tím đậm
                break;
            case 2:
                numberColor = "#06b6d4";  // Cyan space
                glowColor = "#22d3ee";    // Cyan sáng
                ringColor = "#0891b2";    // Cyan đậm
                break;
            case 1:
                numberColor = "#ec4899";  // Hồng neon
                glowColor = "#f472b6";    // Hồng sáng
                ringColor = "#db2777";    // Hồng đậm
                break;
            default:
                numberColor = "#ffffff";
                glowColor = "#e0e0e0";
                ringColor = "#cccccc";
        }

        // ✅ VẼ Ở GIỮA RIGHT PANEL
        double centerX = gameWidth / 2;  // 400px (giữa canvas 800px)
        double centerY = gameHeight / 2; // 300px (giữa canvas 600px)

        gc.save();
        gc.translate(centerX, centerY);
        gc.scale(scale, scale);

        // ===== VÒNG TRÒN XOAY GALAXY =====
        double rotation = (1.0 - fadeProgress) * 360;
        gc.save();
        gc.rotate(rotation);

        // Vẽ 3 vòng tròn đồng tâm với opacity khác nhau
        for (int i = 0; i < 3; i++) {
            double radius = 140 + i * 20;
            double ringOpacity = opacity * (0.3 - i * 0.08);

            gc.setStroke(Color.web(ringColor, ringOpacity));
            gc.setLineWidth(3 - i * 0.5);
            gc.strokeOval(-radius, -radius, radius * 2, radius * 2);
        }
        gc.restore();

        // ===== HIỆU ỨNG GLOW ĐA LỚP =====
        // Lớp glow ngoài cùng (blur lớn)
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web(glowColor, opacity * 0.8),
                80,
                0.7,
                0, 0
        ));
        gc.setFill(Color.web(glowColor, opacity * 0.15));
        gc.fillOval(-110, -110, 220, 220);

        // Lớp glow giữa
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web(numberColor, opacity * 0.9),
                50,
                0.6,
                0, 0
        ));
        gc.setFill(Color.web(numberColor, opacity * 0.25));
        gc.fillOval(-85, -85, 170, 170);

        // ===== SỐ CHÍNH =====
        String numberText = String.valueOf(seconds);

        // Shadow đen cho số (depth)
        gc.setEffect(null);
        gc.setFont(javafx.scene.text.Font.font("Arial Black",
                javafx.scene.text.FontWeight.BOLD, 220));
        gc.setFill(Color.web("#000000", opacity * 0.6));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText(numberText, 6, 8);

        // Outer glow cho số
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web(glowColor, opacity),
                40,
                0.8,
                0, 0
        ));
        gc.setFill(Color.web(numberColor, opacity));
        gc.fillText(numberText, 0, 0);

        // Inner glow (số màu trắng sáng ở giữa)
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web("#ffffff", opacity * 0.9),
                15,
                0.9,
                0, 0
        ));
        gc.setFill(Color.web("#ffffff", opacity * 0.95));
        gc.fillText(numberText, 0, 0);

        // ===== CÁC ĐIỂM SAO NHỎ XUNG QUANH =====
        gc.setEffect(null);
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 + rotation * 0.5);
            double distance = 160 + Math.sin(rotation * 0.05 + i) * 10;
            double x = Math.cos(angle) * distance;
            double y = Math.sin(angle) * distance;
            double starSize = 3 + Math.random() * 2;

            gc.setFill(Color.web(glowColor, opacity * (0.6 + Math.random() * 0.4)));
            gc.fillOval(x - starSize/2, y - starSize/2, starSize, starSize);
        }

        // ===== TEXT MÔ TẢ PHÍA DƯỚI =====
        gc.setEffect(null);
        gc.setFont(javafx.scene.text.Font.font("Arial",
                javafx.scene.text.FontWeight.BOLD, 22));

        String message = isCountdownFromMenu ? "Get Ready!" : "Resume in...";

        // Shadow cho text
        gc.setFill(Color.web("#000000", opacity * 0.7));
        gc.fillText(message, 2, 112);

        // Text chính
        gc.setFill(Color.web("#ffffff", opacity * 0.95));
        gc.fillText(message, 0, 110);

        gc.restore();
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
    public int getLastStreakBonus() {return lastStreakBonus;}
    public boolean isCountdownFromMenu() {return isCountdownFromMenu;}
    // Getter để Ball có thể gọi shake nếu cần
    public CameraShake getCameraShake() {return cameraShake;}
}