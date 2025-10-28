package arkanoid.core;

import arkanoid.entities.Ball.Ball;
import arkanoid.entities.Brick.Brick;
import arkanoid.entities.Brick.NormalBrick;
import arkanoid.entities.Brick.StrongBrick;
import arkanoid.entities.Brick.UnbreakableBrick;
import arkanoid.entities.Paddle.Paddle;
import arkanoid.entities.PowerUp.*;
import arkanoid.utils.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * Quản lý game đối kháng 2 người chơi - FIXED VERSION
 * ✅ Fix: Mỗi player chuyển màn độc lập
 * ✅ Fix: Không countdown ban đầu, cho phép aim trước
 */
public class VersusGameManager {
    private static VersusGameManager instance;

    public enum VersusState {
        WAITING, PLAYING, PAUSED, GAME_OVER
    }

    private VersusState gameState;

    private final double gameWidth = 640;
    private final double gameHeight = 600;

    // ===== PLAYER 1 =====
    private Paddle paddle1;
    private List<Ball> balls1;
    private List<Brick> bricks1;
    private List<PowerUp> powerUps1;
    private List<PowerUp> activePowerUps1;
    private int score1 = 0;
    private int lives1 = 3;
    private boolean player1GameOver = false;
    private int currentLevel1 = 1; // ✅ Level riêng cho P1

    // Player 1 - Streak System
    private int streak1 = 0;
    private boolean collision1 = false;
    private int lastStreakBonus1 = 0;
    private boolean excellentEffect1Active = false;
    private double excellentEffectTimer1 = 0.0;
    private int excellentTriggerStreak1 = 0;

    // Player 1 - Camera Shake
    private CameraShake cameraShake1;

    // Player 1 - Countdown
    private boolean isCountdownActive1 = false;
    private double countdownTime1 = 0;

    // ===== PLAYER 2 =====
    private Paddle paddle2;
    private List<Ball> balls2;
    private List<Brick> bricks2;
    private List<PowerUp> powerUps2;
    private List<PowerUp> activePowerUps2;
    private int score2 = 0;
    private int lives2 = 3;
    private boolean player2GameOver = false;
    private int currentLevel2 = 1; // ✅ Level riêng cho P2

    // Player 2 - Streak System
    private int streak2 = 0;
    private boolean collision2 = false;
    private int lastStreakBonus2 = 0;
    private boolean excellentEffect2Active = false;
    private double excellentEffectTimer2 = 0.0;
    private int excellentTriggerStreak2 = 0;

    // Player 2 - Camera Shake
    private CameraShake cameraShake2;

    // Player 2 - Countdown
    private boolean isCountdownActive2 = false;
    private double countdownTime2 = 0;

    // Input states
    private boolean p1Left = false, p1Right = false;
    private boolean p2Left = false, p2Right = false;

    private static final double DEFAULT_BALL_SPEED = 325.0;

    // Paddle hit cooldown
    private Map<Ball, Long> lastPaddleHitTime1 = new HashMap<>();
    private Map<Ball, Long> lastPaddleHitTime2 = new HashMap<>();
    private static final long PADDLE_HIT_COOLDOWN = 100;

    // Background caching
    private javafx.scene.image.Image cachedBackground = null;
    private boolean backgroundLoaded = false;

    private VersusGameManager() {
        balls1 = new ArrayList<>();
        bricks1 = new ArrayList<>();
        powerUps1 = new ArrayList<>();
        activePowerUps1 = new ArrayList<>();
        cameraShake1 = new CameraShake();

        balls2 = new ArrayList<>();
        bricks2 = new ArrayList<>();
        powerUps2 = new ArrayList<>();
        activePowerUps2 = new ArrayList<>();
        cameraShake2 = new CameraShake();

        gameState = VersusState.WAITING;
    }

    public static VersusGameManager getInstance() {
        if (instance == null) {
            instance = new VersusGameManager();
        }
        return instance;
    }

    public void startNewGame() {
        resetGame();
        gameState = VersusState.PLAYING;

        // ✅ FIX: KHÔNG start countdown ngay
        // ✅ Để ball stick trên paddle, player có thể aim (A/D) rồi fire (W/UP)
        System.out.println("✅ Game started - Players can aim with A/D and LEFT/RIGHT, fire with W/UP");
    }

    private void resetGame() {
        score1 = 0;
        lives1 = 3;
        player1GameOver = false;
        streak1 = 0;
        collision1 = false;
        lastStreakBonus1 = 0;
        excellentEffect1Active = false;
        lastPaddleHitTime1.clear();
        currentLevel1 = 1; // ✅ Reset level P1

        score2 = 0;
        lives2 = 3;
        player2GameOver = false;
        streak2 = 0;
        collision2 = false;
        lastStreakBonus2 = 0;
        excellentEffect2Active = false;
        lastPaddleHitTime2.clear();
        currentLevel2 = 1; // ✅ Reset level P2

        p1Left = p1Right = false;
        p2Left = p2Right = false;

        paddle1 = new Paddle(gameWidth / 2 - 60, gameHeight - 50);
        balls1.clear();
        Ball ball1 = new Ball(gameWidth / 2 - 15, gameHeight - 100);
        ball1.applySpeed(DEFAULT_BALL_SPEED);
        ball1.stickToPaddle(paddle1); // ✅ Stick để aim
        balls1.add(ball1);

        paddle2 = new Paddle(gameWidth / 2 - 60, gameHeight - 50);
        balls2.clear();
        Ball ball2 = new Ball(gameWidth / 2 - 15, gameHeight - 100);
        ball2.applySpeed(DEFAULT_BALL_SPEED);
        ball2.stickToPaddle(paddle2); // ✅ Stick để aim
        balls2.add(ball2);

        bricks1.clear();
        powerUps1.clear();
        activePowerUps1.clear();

        bricks2.clear();
        powerUps2.clear();
        activePowerUps2.clear();

        loadLevel(1, bricks1); // Load level 1 cho P1
        loadLevel(1, bricks2); // Load level 1 cho P2
    }

    /**
     * ✅ FIX: Load level cho TỪNG player riêng biệt
     */
    private void loadLevel(int level, List<Brick> bricks) {
        String filename = "map" + Math.min(level, 9) + ".csv";
        loadBricksForPlayer(bricks, filename);
        System.out.println("✅ Loaded " + filename + " for player (level " + level + ")");
    }

    private void loadBricksForPlayer(List<Brick> bricks, String filename) {
        bricks.clear();

        try {
            java.nio.file.Path file = java.nio.file.Paths.get("src", "arkanoid", "assets", "maps", filename).normalize();

            if (!java.nio.file.Files.isRegularFile(file)) {
                file = java.nio.file.Paths.get("..", "src", "arkanoid", "assets", "maps", filename).normalize();
            }

            try (Scanner scanner = new Scanner(
                    java.nio.file.Files.newBufferedReader(file, java.nio.charset.StandardCharsets.UTF_8))) {

                int row = 0;
                double brickWidth = gameWidth / 10.0;
                double brickHeight = 25.0;

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] values = line.split(",", -1);

                    for (int col = 0; col < values.length; col++) {
                        String raw = values[col].trim();
                        if (raw.isEmpty()) continue;

                        int type;
                        try {
                            type = Integer.parseInt(raw);
                        } catch (NumberFormatException ex) {
                            continue;
                        }

                        if (type == 0) continue;

                        double x = col * brickWidth;
                        double y = 50 + row * brickHeight;

                        switch (type) {
                            case 1 -> bricks.add(new NormalBrick(x, y, brickWidth - 2, brickHeight - 2));
                            case 2 -> bricks.add(new StrongBrick(x, y, brickWidth - 2, brickHeight - 2));
                            case 3 -> bricks.add(new UnbreakableBrick(x, y, brickWidth - 2, brickHeight - 2));
                        }
                    }
                    row++;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Cannot load " + filename + ": " + e.getMessage());
        }
    }

    public void update(double deltaTime) {
        if (gameState != VersusState.PLAYING) return;

        // Update countdown cho player 1
//        if (isCountdownActive1) {
//            countdownTime1 -= deltaTime;
//            if (countdownTime1 <= 0) {
//                isCountdownActive1 = false;
//                for (Ball b : balls1) {
//                    b.release();
//                }
//            }
//        }
//
//        // Update countdown cho player 2
//        if (isCountdownActive2) {
//            countdownTime2 -= deltaTime;
//            if (countdownTime2 <= 0) {
//                isCountdownActive2 = false;
//                for (Ball b : balls2) {
//                    b.release();
//                }
//            }
//        }

        if (!player1GameOver && !isCountdownActive1) {
            updatePlayer(deltaTime, paddle1, balls1, bricks1, powerUps1, activePowerUps1,
                    p1Left, p1Right, 1);
        }

        if (!player2GameOver && !isCountdownActive2) {
            updatePlayer(deltaTime, paddle2, balls2, bricks2, powerUps2, activePowerUps2,
                    p2Left, p2Right, 2);
        }

        // Update camera shake
        cameraShake1.update(deltaTime);
        cameraShake2.update(deltaTime);

        // Update excellent effects
        if (excellentEffect1Active) {
            excellentEffectTimer1 += deltaTime;
            if (excellentEffectTimer1 > 2.0) {
                excellentEffect1Active = false;
            }
        }

        if (excellentEffect2Active) {
            excellentEffectTimer2 += deltaTime;
            if (excellentEffectTimer2 > 2.0) {
                excellentEffect2Active = false;
            }
        }

        if (player1GameOver && player2GameOver) {
            gameState = VersusState.GAME_OVER;
            System.out.println("🏁 GAME OVER - P1: " + score1 + " | P2: " + score2);
        }
    }

    private void updatePlayer(double deltaTime, Paddle paddle, List<Ball> balls, List<Brick> bricks,
                              List<PowerUp> powerUps, List<PowerUp> activePowerUps,
                              boolean movingLeft, boolean movingRight, int playerNum) {

        paddle.update(deltaTime);

        boolean anyBallStuck = balls.stream().anyMatch(Ball::isStuckToPaddle);

        if (!anyBallStuck) {
            if (movingLeft) {
                double newX = paddle.getX() - paddle.getSpeed() * deltaTime;
                if (newX < 0) newX = 0;
                paddle.setX(newX);
            }
            if (movingRight) {
                double newX = paddle.getX() + paddle.getSpeed() * deltaTime;
                if (newX + paddle.getWidth() > gameWidth) {
                    newX = gameWidth - paddle.getWidth();
                }
                paddle.setX(newX);
            }
        }

        for (Ball ball : balls) {
            if (ball.isStuckToPaddle()) {
                ball.updateStuckPosition(paddle);
            }
        }

        for (Ball ball : balls) {
            ball.update(deltaTime);
        }

        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);
        }

        activePowerUps.removeIf(powerUp -> {
            powerUp.update(deltaTime);
            if (powerUp.isExpired()) {
                powerUp.removeEffect(paddle);
                return true;
            }
            return false;
        });

        checkCollisionsForPlayer(paddle, balls, bricks, powerUps, activePowerUps, playerNum);
    }

    private void checkCollisionsForPlayer(Paddle paddle, List<Ball> balls, List<Brick> bricks,
                                          List<PowerUp> powerUps, List<PowerUp> activePowerUps,
                                          int playerNum) {

        Iterator<Ball> ballIterator = balls.iterator();
        boolean inMoment = false;
        boolean paddleHitThisFrame = false;

        // Get player-specific data
        Map<Ball, Long> lastPaddleHitTime = (playerNum == 1) ? lastPaddleHitTime1 : lastPaddleHitTime2;
        CameraShake cameraShake = (playerNum == 1) ? cameraShake1 : cameraShake2;

        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();

            if (ball.isStuckToPaddle()) continue;

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

            if (ball.getY() > gameHeight) {
                ballIterator.remove();
                lastPaddleHitTime.remove(ball);

                // Reset streak when ball falls
                if (playerNum == 1) {
                    collision1 = false;
                    streak1 = 0;
                } else {
                    collision2 = false;
                    streak2 = 0;
                }

                if (balls.isEmpty()) {
                    if (playerNum == 1) {
                        lives1--;
                        if (lives1 <= 0) {
                            player1GameOver = true;
                            SoundManager.play("matmang.wav");
                        } else {
                            resetBallForPlayer(paddle, balls);
                            startCountdownForPlayer(playerNum, 3.0);
                        }
                    } else {
                        lives2--;
                        if (lives2 <= 0) {
                            player2GameOver = true;
                            SoundManager.play("matmang.wav");
                        } else {
                            resetBallForPlayer(paddle, balls);
                            startCountdownForPlayer(playerNum, 3.0);
                        }
                    }
                }
                continue;
            }

            // Ball-Paddle collision with cooldown
            if (ball.intersects(paddle)) {
                long currentTime = System.currentTimeMillis();
                Long lastHit = lastPaddleHitTime.get(ball);

                if (ball.getDY() > 0 &&
                        (lastHit == null || currentTime - lastHit > PADDLE_HIT_COOLDOWN)) {

                    ball.bounceOffPaddle(paddle);
                    paddle.triggerHitAnimation();
                    cameraShake.shakeOnPaddleHit();

                    // Reset collision flag
                    if (playerNum == 1) {
                        collision1 = false;
                    } else {
                        collision2 = false;
                    }

                    lastPaddleHitTime.put(ball, currentTime);

                    if (!paddleHitThisFrame) {
                        SoundManager.play("paddle.wav");
                        paddleHitThisFrame = true;
                    }
                }
            }

            // Ball-Brick collisions
            for (Brick brick : bricks) {
                if (ball.intersects(brick)) {
                    ball.bounceOff(brick);
                    brick.takeHit();

                    if (brick.getType() != Brick.BrickType.UNBREAKABLE) {
                        if (playerNum == 1) {
                            collision1 = true;
                        } else {
                            collision2 = true;
                        }
                        inMoment = true;
                    }

                    if (brick.isDestroyed()) {
                        cameraShake.shakeOnBrickHit();
                        SoundManager.play("gachvo.wav");

                        if (playerNum == 1) {
                            score1 += brick.getPoints();
                        } else {
                            score2 += brick.getPoints();
                        }

                        if (Math.random() < 0.25) {
                            spawnRandomPowerUp(brick.getX() + brick.getWidth() / 2,
                                    brick.getY() + brick.getHeight(), powerUps);
                        }
                    } else {
                        cameraShake.shakeOnPaddleHit();
                        SoundManager.play("gach.wav");
                    }
                    break;
                }
            }
        }

        // ===== STREAK SYSTEM =====
        if (inMoment) {
            if (playerNum == 1) {
                if (collision1 == false) {
                    streak1 = 0;
                    lastStreakBonus1 = 0;
                } else {
                    streak1++;
                    System.out.println("P1 Streak: " + streak1);

                    if (streak1 >= 4) {
                        int streakBonus = 5 * (streak1 / 4);
                        lastStreakBonus1 = streakBonus;
                        score1 += streakBonus;

                        if (streak1 % 4 == 0 && !excellentEffect1Active) {
                            triggerExcellentEffect(1, streak1);
                        }
                    }
                }
            } else {
                if (collision2 == false) {
                    streak2 = 0;
                    lastStreakBonus2 = 0;
                } else {
                    streak2++;
                    System.out.println("P2 Streak: " + streak2);

                    if (streak2 >= 4) {
                        int streakBonus = 5 * (streak2 / 4);
                        lastStreakBonus2 = streakBonus;
                        score2 += streakBonus;

                        if (streak2 % 4 == 0 && !excellentEffect2Active) {
                            triggerExcellentEffect(2, streak2);
                        }
                    }
                }
            }
        }

        bricks.removeIf(Brick::isDestroyed);

        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            if (paddle.intersects(powerUp)) {
                applyPowerUpEffect(powerUp, paddle, balls, activePowerUps, playerNum);
                SoundManager.play("powerup.wav");
                powerUpIterator.remove();
            } else if (powerUp.getY() > gameHeight) {
                powerUpIterator.remove();
            }
        }

        // ✅ FIX: Check level complete cho TỪNG player
        checkLevelCompleteForPlayer(bricks, paddle, balls, playerNum);
    }

    /**
     * ✅ FIX: Check level complete riêng cho từng player
     */
    private void checkLevelCompleteForPlayer(List<Brick> bricks, Paddle paddle, List<Ball> balls, int playerNum) {
        boolean cleared = bricks.stream()
                .noneMatch(brick -> !brick.isDestroyed() && brick.getType() != Brick.BrickType.UNBREAKABLE);

        if (cleared) {
            if (playerNum == 1 && !player1GameOver) {
                currentLevel1++;
                System.out.println("✅ P1 cleared level " + (currentLevel1 - 1) + " → Loading level " + currentLevel1);
                SoundManager.play("Qua_man.wav");
                loadLevel(currentLevel1, bricks1);
                resetBallForPlayer(paddle1, balls1);
                startCountdownForPlayer(1, 3.0);
            } else if (playerNum == 2 && !player2GameOver) {
                currentLevel2++;
                System.out.println("✅ P2 cleared level " + (currentLevel2 - 1) + " → Loading level " + currentLevel2);
                SoundManager.play("Qua_man.wav");
                loadLevel(currentLevel2, bricks2);
                resetBallForPlayer(paddle2, balls2);
                startCountdownForPlayer(2, 3.0);
            }
        }
    }

    private void triggerExcellentEffect(int playerNum, int streak) {
        if (playerNum == 1) {
            excellentEffect1Active = true;
            excellentEffectTimer1 = 0.0;
            excellentTriggerStreak1 = streak;
        } else {
            excellentEffect2Active = true;
            excellentEffectTimer2 = 0.0;
            excellentTriggerStreak2 = streak;
        }

        System.out.println("EXCELLENT TRIGGERED! P" + playerNum + " Streak: " + streak);
        SoundManager.play("streak.wav");

        new Thread(() -> {
            try {
                Thread.sleep(750);
                SoundManager.play("goodjob.wav");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startCountdownForPlayer(int playerNum, double seconds) {
        if (playerNum == 1) {
            //isCountdownActive1 = true;
            //countdownTime1 = seconds;
            for (Ball b : balls1) {
                b.stickToPaddle(paddle1);
            }
        } else {
            //isCountdownActive2 = true;
            //countdownTime2 = seconds;
            for (Ball b : balls2) {
                b.stickToPaddle(paddle2);
            }
        }
    }

    private void resetBallForPlayer(Paddle paddle, List<Ball> balls) {
        balls.clear();
        Ball ball = new Ball(paddle.getX() + paddle.getWidth() / 2 - 15, gameHeight - 100);
        ball.applySpeed(DEFAULT_BALL_SPEED);
        ball.stickToPaddle(paddle);
        balls.add(ball);
    }

    private void spawnRandomPowerUp(double x, double y, List<PowerUp> powerUps) {
        double rand = Math.random();
        PowerUp powerUp;

        if (rand < 0.3) {
            powerUp = new ExpandPaddlePowerUp(x, y);
        } else if (rand < 0.55) {
            powerUp = new SlowBallPowerUp(x, y);
        } else if (rand < 0.8) {
            powerUp = new MultiBallPowerUp(x, y);
        } else {
            powerUp = new ExtraLifePowerUp(x, y);
        }

        powerUps.add(powerUp);
    }

    private void applyPowerUpEffect(PowerUp powerUp, Paddle paddle, List<Ball> balls,
                                    List<PowerUp> activePowerUps, int playerNum) {
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

            case SLOW_BALL:
                for (Ball b : balls) {
                    b.applySpeed(DEFAULT_BALL_SPEED * 0.7);
                }
                powerUp.activate();
                activePowerUps.add(powerUp);
                break;

            case EXTRA_LIFE:
                if (playerNum == 1 && lives1 < 3) {
                    lives1++;
                } else if (playerNum == 2 && lives2 < 3) {
                    lives2++;
                }
                break;

            case MULTI_BALL:
                spawnMultiBalls(balls);
                break;
        }
    }

    private void spawnMultiBalls(List<Ball> balls) {
        if (balls.isEmpty()) return;

        Ball original = balls.get(0);
        if (original.isStuckToPaddle()) return;

        for (int i = 0; i < 2; i++) {
            Ball newBall = new Ball(original.getX(), original.getY());
            newBall.setTypeSkin(original.getTypeSkin());
            newBall.applySpeed(original.getSpeed());

            double angle = Math.toRadians(-60 + i * 60);
            double dx = Math.sin(angle);
            double dy = -Math.cos(angle);
            newBall.setDirection(dx, dy);
            newBall.release();

            balls.add(newBall);
        }
    }

    // ✅ RENDER TRỰC TIẾP - KHÔNG GỌI entity.render()
    public void renderPlayer1(GraphicsContext gc) {
        renderPlayer(gc, paddle1, balls1, bricks1, powerUps1, player1GameOver, 1);
    }

    public void renderPlayer2(GraphicsContext gc) {
        renderPlayer(gc, paddle2, balls2, bricks2, powerUps2, player2GameOver, 2);
    }

    private void renderPlayer(GraphicsContext gc, Paddle paddle, List<Ball> balls,
                              List<Brick> bricks, List<PowerUp> powerUps, boolean gameOver, int playerNum) {

        // Load background nếu chưa load
        if (!backgroundLoaded) {
            loadBackgroundImage();
        }

        // Get camera shake offset
        CameraShake cameraShake = (playerNum == 1) ? cameraShake1 : cameraShake2;
        double shakeX = cameraShake.getShakeX();
        double shakeY = cameraShake.getShakeY();

        gc.save();
        gc.translate(shakeX, shakeY);

        // ===== Vẽ BACKGROUND =====
        if (cachedBackground != null) {
            gc.drawImage(cachedBackground, -shakeX, -shakeY, gameWidth, gameHeight);
        } else {
            // Fallback: clear với màu đơn sắc nếu không có background
            gc.setFill(Color.web("#0f172a"));
            gc.fillRect(-shakeX, -shakeY, gameWidth, gameHeight);
        }

        // Border
        gc.setStroke(Color.web("#0ea5e9"));
        gc.setLineWidth(2);
        gc.strokeRect(1, 1, gameWidth - 2, gameHeight - 2);

        if (gameOver) {
            gc.setFill(Color.web("#ef4444"));
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 36));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText("GAME OVER", gameWidth / 2, gameHeight / 2);
            gc.restore();
            return;
        }

        // ===== RENDER PADDLE =====
        paddle.render(gc);

        // ===== RENDER BALLS =====
        for (Ball ball : balls) {
            ball.render(gc);

            // ✅ Hiển thị arrow AIM khi ball đang stuck
            if (ball.isStuckToPaddle()) {
                ball.renderDirectionArrow(gc);
            }
        }

        // ✅ RENDER BRICKS
        for (Brick brick : bricks) {
            brick.render(gc);
        }

        // ✅ RENDER POWERUPS
        for (PowerUp powerUp : powerUps) {
            powerUp.render(gc);
        }

        gc.restore();

        // ===== RENDER EXCELLENT EFFECT =====
        if (playerNum == 1 && excellentEffect1Active) {
            renderExcellentEffect(gc, excellentEffectTimer1, excellentTriggerStreak1);
        } else if (playerNum == 2 && excellentEffect2Active) {
            renderExcellentEffect(gc, excellentEffectTimer2, excellentTriggerStreak2);
        }

        // ===== RENDER COUNTDOWN =====
//        boolean isCountdownActive = (playerNum == 1) ? isCountdownActive1 : isCountdownActive2;
//        double countdownTime = (playerNum == 1) ? countdownTime1 : countdownTime2;
//
//        if (isCountdownActive) {
//            renderCountdown(gc, countdownTime);
//        }
    }

    /**
     * ✅ Load background image (chỉ load 1 lần)
     */
    private void loadBackgroundImage() {
        if (backgroundLoaded) return;

        try {
            String resourcePath = "/assets/images/bg-retrospace(1).png";
            var resourceStream = getClass().getResourceAsStream(resourcePath);

            if (resourceStream != null) {
                cachedBackground = new javafx.scene.image.Image(resourceStream);
                System.out.println("✅ Background cached from resources");
            } else {
                // Fallback: load từ file
                java.io.File imageFile = new java.io.File("src/arkanoid/assets/images/bg-retrospace(1).png");
                if (imageFile.exists()) {
                    cachedBackground = new javafx.scene.image.Image(imageFile.toURI().toString());
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
     * ✅ Render Excellent Effect
     */
    private void renderExcellentEffect(GraphicsContext gc, double excellentEffectTimer, int excellentTriggerStreak) {
        double progress = excellentEffectTimer / 2.0;
        double alpha = 0.0;
        double slideX = 0.0;

        if (excellentEffectTimer < 0.6) {
            double phaseProgress = excellentEffectTimer / 0.6;
            alpha = phaseProgress;
            slideX = (1 - phaseProgress) * 100;
        } else if (excellentEffectTimer < 1.2) {
            alpha = 1.0;
            slideX = 0.0;
        } else {
            double phaseProgress = (excellentEffectTimer - 1.2) / 0.8;
            alpha = 1.0 - phaseProgress;
            slideX = phaseProgress * 100;
        }

        double canvasHeight = gc.getCanvas().getHeight();
        double canvasWidth = gc.getCanvas().getWidth();

        double boxWidth = 140;
        double boxHeight = 50;

        double rightMarginPercent = 0.25;
        double heightPercent = 0.6;

        double x = canvasWidth * (1 - rightMarginPercent) + slideX;
        double y = canvasHeight * heightPercent;

        gc.save();

        // ===== BACKGROUND BOX =====
        gc.setFill(Color.web("#000000", alpha * 0.3));
        gc.fillRoundRect(x - boxWidth/2, y - boxHeight/2, boxWidth, boxHeight, 10, 10);

        // ===== VIỀN VÀNG ĐẸP HƠN - GLOW MULTIPLE LAYERS =====
        gc.setStroke(Color.web("#fbbf24", alpha * 0.7));
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(x - boxWidth/2, y - boxHeight/2, boxWidth, boxHeight, 10, 10);

        gc.setStroke(Color.web("#f59e0b", alpha * 0.6));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x - boxWidth/2 + 1, y - boxHeight/2 + 1, boxWidth - 2, boxHeight - 2, 9, 9);

        // ===== GLOW EFFECT =====
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

        gc.setFill(Color.web("#000000", alpha * 0.5));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText("EXCELLENT!", x + 1, y + 2);

        gc.setFill(Color.web("#fbbf24", alpha * 0.9));
        gc.fillText("EXCELLENT!", x, y);

        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web("#ffffff", alpha * 0.5),
                8,
                0.8,
                0, 0
        ));
        gc.setFill(Color.web("#ffffff", alpha * 0.3));
        gc.fillText("EXCELLENT!", x, y);

        // ===== STREAK INFO =====
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web("#ff6b00", alpha * 0.7),
                12,
                0.7,
                0, 0
        ));
        gc.setFont(javafx.scene.text.Font.font("Arial Black",
                javafx.scene.text.FontWeight.BOLD, 16));

        gc.setFill(Color.web("#cc3300", alpha * 0.6));
        gc.fillText("x" + excellentTriggerStreak + " Streak", x + 1, y + 23);

        gc.setFill(Color.web("#ff6b00", alpha * 0.85));
        gc.fillText("x" + excellentTriggerStreak + " Streak", x, y + 22);

        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web("#ffaa33", alpha * 0.6),
                7,
                0.8,
                0, 0
        ));
        gc.setFill(Color.web("#ffaa33", alpha * 0.5));
        gc.fillText("x" + excellentTriggerStreak + " Streak", x, y + 22);

        // ===== SHINE EFFECT =====
        if (excellentEffectTimer < 1.2) {
            double shineProgress = excellentEffectTimer / 1.2;
            double shineX = x - boxWidth/2 + shineProgress * boxWidth;

            gc.setEffect(null);

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
     * ✅ Render Countdown
     */
    private void renderCountdown(GraphicsContext gc, double countdownTime) {
        int seconds = (int) Math.ceil(countdownTime);

        // OVERLAY
        gc.setFill(Color.web("#000000", 0.75));
        gc.fillRect(0, 0, gameWidth, gameHeight);

        double fadeProgress = countdownTime - Math.floor(countdownTime);
        double scale = 0.7 + (1.0 - fadeProgress) * 0.5;
        double opacity = 0.2 + fadeProgress * 0.8;

        String numberColor, glowColor, ringColor;
        switch (seconds) {
            case 3:
                numberColor = "#a855f7";
                glowColor = "#c084fc";
                ringColor = "#7c3aed";
                break;
            case 2:
                numberColor = "#06b6d4";
                glowColor = "#22d3ee";
                ringColor = "#0891b2";
                break;
            case 1:
                numberColor = "#ec4899";
                glowColor = "#f472b6";
                ringColor = "#db2777";
                break;
            default:
                numberColor = "#ffffff";
                glowColor = "#e0e0e0";
                ringColor = "#cccccc";
        }

        double centerX = gameWidth / 2;
        double centerY = gameHeight / 2;

        gc.save();
        gc.translate(centerX, centerY);
        gc.scale(scale, scale);

        // ===== VÒNG TRÒN XOAY =====
        double rotation = (1.0 - fadeProgress) * 360;
        gc.save();
        gc.rotate(rotation);

        for (int i = 0; i < 3; i++) {
            double radius = 140 + i * 20;
            double ringOpacity = opacity * (0.3 - i * 0.08);

            gc.setStroke(Color.web(ringColor, ringOpacity));
            gc.setLineWidth(3 - i * 0.5);
            gc.strokeOval(-radius, -radius, radius * 2, radius * 2);
        }
        gc.restore();

        // ===== GLOW ĐA LỚP =====
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web(glowColor, opacity * 0.8),
                80,
                0.7,
                0, 0
        ));
        gc.setFill(Color.web(glowColor, opacity * 0.15));
        gc.fillOval(-110, -110, 220, 220);

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

        gc.setEffect(null);
        gc.setFont(javafx.scene.text.Font.font("Arial Black",
                javafx.scene.text.FontWeight.BOLD, 220));
        gc.setFill(Color.web("#000000", opacity * 0.6));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText(numberText, 6, 8);

        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web(glowColor, opacity),
                40,
                0.8,
                0, 0
        ));
        gc.setFill(Color.web(numberColor, opacity));
        gc.fillText(numberText, 0, 0);

        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                Color.web("#ffffff", opacity * 0.9),
                15,
                0.9,
                0, 0
        ));
        gc.setFill(Color.web("#ffffff", opacity * 0.95));
        gc.fillText(numberText, 0, 0);

        // ===== ĐIỂM SAO XUNG QUANH =====
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

        // ===== TEXT MÔ TẢ =====
        gc.setEffect(null);
        gc.setFont(javafx.scene.text.Font.font("Arial",
                javafx.scene.text.FontWeight.BOLD, 22));

        String message = "Get Ready!";

        gc.setFill(Color.web("#000000", opacity * 0.7));
        gc.fillText(message, 2, 112);

        gc.setFill(Color.web("#ffffff", opacity * 0.95));
        gc.fillText(message, 0, 110);

        gc.restore();
    }

    // INPUT HANDLERS
    public void setPlayer1Left(boolean pressed) { this.p1Left = pressed; }
    public void setPlayer1Right(boolean pressed) { this.p1Right = pressed; }
    public void setPlayer2Left(boolean pressed) { this.p2Left = pressed; }
    public void setPlayer2Right(boolean pressed) { this.p2Right = pressed; }

    public void rotatePlayer1Ball(boolean clockwise) {
        for (Ball ball : balls1) {
            if (ball.isStuckToPaddle()) {
                ball.rotateDirection(clockwise);
            }
        }
    }

    public void rotatePlayer2Ball(boolean clockwise) {
        for (Ball ball : balls2) {
            if (ball.isStuckToPaddle()) {
                ball.rotateDirection(clockwise);
            }
        }
    }

    public void firePlayer1Ball() {
        if (!isCountdownActive1) {
            for (Ball ball : balls1) {
                if (ball.isStuckToPaddle()) {
                    ball.applySelectedDirection();
                    ball.release();
                }
            }
        }
    }

    public void firePlayer2Ball() {
        if (!isCountdownActive2) {
            for (Ball ball : balls2) {
                if (ball.isStuckToPaddle()) {
                    ball.applySelectedDirection();
                    ball.release();
                }
            }
        }
    }

    public void togglePause() {
        if (gameState == VersusState.PLAYING) {
            gameState = VersusState.PAUSED;
        } else if (gameState == VersusState.PAUSED) {
            gameState = VersusState.PLAYING;
        }
    }

    // GETTERS
    public VersusState getGameState() { return gameState; }
    public int getScore1() { return score1; }
    public int getScore2() { return score2; }
    public int getLives1() { return lives1; }
    public int getLives2() { return lives2; }
    public boolean isPlayer1GameOver() { return player1GameOver; }
    public boolean isPlayer2GameOver() { return player2GameOver; }
    public int getStreak1() { return streak1; }
    public int getStreak2() { return streak2; }
    public int getLastStreakBonus1() { return lastStreakBonus1; }
    public int getLastStreakBonus2() { return lastStreakBonus2; }
    public int getCurrentLevel1() { return currentLevel1; }
    public int getCurrentLevel2() { return currentLevel2; }

    public String getWinner() {
        if (score1 > score2) return "P1";
        if (score2 > score1) return "P2";
        return "DRAW";
    }

    public List<Ball> getBalls1() { return balls1; }
    public List<Ball> getBalls2() { return balls2; }
    public Paddle getPaddle1() { return paddle1; }
    public Paddle getPaddle2() { return paddle2; }
    public List<Brick> getBricks1() { return bricks1; }
    public List<Brick> getBricks2() { return bricks2; }

    public static void resetInstance() {
        instance = null;
    }
}