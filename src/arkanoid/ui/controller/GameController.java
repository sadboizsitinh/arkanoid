package arkanoid.ui.controller;

import arkanoid.core.GameManager;
import arkanoid.core.HighScoreManager;
import arkanoid.entities.PowerUp.PowerUp;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import arkanoid.utils.ScoreAnimation;
import javafx.scene.layout.StackPane;

public class GameController {

    @FXML
    private Canvas gameCanvas;

    @FXML
    private Button btnPause;

    @FXML
    private javafx.scene.layout.StackPane pauseOverlay;

    @FXML
    private PauseOverlayController pauseOverlayController;

    // UI Labels for left panel
    @FXML
    private Label lblScore;

    @FXML
    private Label lblLives;

    @FXML
    private Label lblLevel;

    @FXML
    private Label lblBalls;

    @FXML
    private VBox ballsContainer;

    @FXML
    private VBox powerUpsContainer;

    @FXML
    private Label lblNoPowerUps;

    @FXML
    private HBox rootPane;

    @FXML
    private StackPane scoreContainer;

    private static GameController lastInstance;
    private GameManager.GameState lastState = null;

    private GameManager gameManager;
    private AnimationTimer gameLoop;

    // Track key states
    private boolean leftKeyDown = false;
    private boolean rightKeyDown = false;

    // Flag để tránh hiển thị Game Over nhiều lần
    private boolean gameOverShown = false;
    private int lastScore = 0;

    @FXML
    private void initialize() {
        lastInstance = this;
        lastScore = 0;
        System.out.println("GameController initialize called");
        System.out.println("Canvas size: " + gameCanvas.getWidth() + "x" + gameCanvas.getHeight());

        // Ẩn overlay lúc đầu
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
            pauseOverlay.setMouseTransparent(true);
        }

        gameManager = GameManager.getInstance();

        if (gameManager.hasSavedGame()) {
            System.out.println("Continuing from saved game...");
        } else {
            System.out.println("Starting new game...");
            gameManager.setGameState(GameManager.GameState.MENU);
            gameManager.startGame();
        }

        System.out.println("Game state after init: " + gameManager.getGameState());

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gameCanvas.setFocusTraversable(true);

        if (rootPane != null) {
            BackgroundHelper.setBackgroundImage(rootPane, "bg-retrospace.png");
            System.out.println("Background set for game view");
        }
        javafx.application.Platform.runLater(() -> {
            gameCanvas.requestFocus();
        });
        // Bắt đầu game loop
        startGameLoop(gc);

        // Setup input handlers
        gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                System.out.println("Scene ready, setting up input handlers");

                newScene.setOnKeyPressed(event -> {
                    handleKeyPress(event.getCode(), true);
                    event.consume();
                });

                newScene.setOnKeyReleased(event -> {
                    handleKeyPress(event.getCode(), false);
                    event.consume();
                });

                gameCanvas.requestFocus();
            }
        });

        // Nút Pause
        if (btnPause != null) {
            btnPause.setOnAction(e -> {
                gameManager.togglePause();
                gameCanvas.requestFocus();
            });
        }

        javafx.application.Platform.runLater(() -> {
            gameCanvas.requestFocus();
            System.out.println("Focus requested");

            // THÊM: Điều chỉnh font size ngay khi khởi tạo
            if (lblScore != null) {
                adjustScoreFontSize(gameManager.getScore());
            }
        });
    }

    private void handleKeyPress(KeyCode code, boolean isPressed) {
        // ========== KIỂM TRA BÓNG CÓ DÍNH TRÊN PADDLE KHÔNG ==========
        boolean ballStuck = gameManager.getBalls().stream()
                .anyMatch(ball -> ball.isStuckToPaddle());

        // ========== XỬ LÝ PHÍM A - DI CHUYỂN/XOAY GÓC ==========
        if (code == KeyCode.A) {
            if (isPressed) {
                if (ballStuck) {
                    // Nếu bóng dính → XOAY GÓC TRÁI
                    gameManager.rotateSelectedBallDirection(false);
                    System.out.println("🎮 A pressed: Rotate LEFT");
                } else {
                    // Nếu bóng bay → DI CHUYỂN PADDLE TRÁI
                    if (!leftKeyDown) {
                        leftKeyDown = true;
                        gameManager.setMovingLeft(true);
                        System.out.println("🎮 A pressed: Move LEFT");
                    }
                }
            } else {
                // A nhả ra
                if (!ballStuck && leftKeyDown) {
                    leftKeyDown = false;
                    gameManager.setMovingLeft(false);
                    System.out.println("🎮 A released: Stop LEFT");
                }
            }
        }

        // ========== XỬ LÝ PHÍM D - DI CHUYỂN/XOAY GÓC ==========
        if (code == KeyCode.D) {
            if (isPressed) {
                if (ballStuck) {
                    // Nếu bóng dính → XOAY GÓC PHẢI
                    gameManager.rotateSelectedBallDirection(true);
                    System.out.println("🎮 D pressed: Rotate RIGHT");
                } else {
                    // Nếu bóng bay → DI CHUYỂN PADDLE PHẢI
                    if (!rightKeyDown) {
                        rightKeyDown = true;
                        gameManager.setMovingRight(true);
                        System.out.println("🎮 D pressed: Move RIGHT");
                    }
                }
            } else {
                // D nhả ra
                if (!ballStuck && rightKeyDown) {
                    rightKeyDown = false;
                    gameManager.setMovingRight(false);
                    System.out.println("🎮 D released: Stop RIGHT");
                }
            }
        }

        // ========== XỬ LÝ PHÍM MŨI TÊN TRÁI (←) ==========
        if (code == KeyCode.LEFT) {
            if (isPressed) {
                if (ballStuck) {
                    // Nếu bóng dính → XOAY GÓC TRÁI
                    gameManager.rotateSelectedBallDirection(false);
                    System.out.println("🎮 LEFT arrow pressed: Rotate LEFT");
                } else {
                    // Nếu bóng bay → DI CHUYỂN PADDLE TRÁI
                    if (!leftKeyDown) {
                        leftKeyDown = true;
                        gameManager.setMovingLeft(true);
                        System.out.println("🎮 LEFT arrow pressed: Move LEFT");
                    }
                }
            } else {
                // LEFT nhả ra
                if (!ballStuck && leftKeyDown) {
                    leftKeyDown = false;
                    gameManager.setMovingLeft(false);
                    System.out.println("🎮 LEFT arrow released: Stop LEFT");
                }
            }
        }

        // ========== XỬ LÝ PHÍM MŨI TÊN PHẢI (→) ==========
        if (code == KeyCode.RIGHT) {
            if (isPressed) {
                if (ballStuck) {
                    // Nếu bóng dính → XOAY GÓC PHẢI
                    gameManager.rotateSelectedBallDirection(true);
                    System.out.println("🎮 RIGHT arrow pressed: Rotate RIGHT");
                } else {
                    // Nếu bóng bay → DI CHUYỂN PADDLE PHẢI
                    if (!rightKeyDown) {
                        rightKeyDown = true;
                        gameManager.setMovingRight(true);
                        System.out.println("🎮 RIGHT arrow pressed: Move RIGHT");
                    }
                }
            } else {
                // RIGHT nhả ra
                if (!ballStuck && rightKeyDown) {
                    rightKeyDown = false;
                    gameManager.setMovingRight(false);
                    System.out.println("🎮 RIGHT arrow released: Stop RIGHT");
                }
            }
        }

        // ========== XỬ LÝ PHÍM SPACE - PHÓNG BÓNG ==========
        if (isPressed && code == KeyCode.SPACE) {
            if (ballStuck) {
                // Nếu bóng dính → PHÓNG BÓNG
                gameManager.fireSelectedBallDirection();
                System.out.println("🎮 SPACE pressed: FIRE BALL! 🔥");
            } else if (gameManager.getGameState() == GameManager.GameState.MENU) {
                // Nếu ở menu → BẮT ĐẦU GAME
                gameManager.startGame();
                System.out.println("🎮 SPACE pressed: Start Game");
            }
        }

        // ========== XỬ LÝ PHÍM P - TẠM DỪNG ==========
        if (isPressed && (code == KeyCode.P || code == KeyCode.ESCAPE)) {
            gameManager.togglePause();
            System.out.println("🎮 P/ESC pressed: Toggle Pause");
        }

        // ========== XỬ LÝ PHÍM R - RESTART ==========
        if (isPressed && code == KeyCode.R && gameManager.getGameState() == GameManager.GameState.GAME_OVER) {
            gameManager.startGame();
            System.out.println("🎮 R pressed: Restart Game");
        }
    }

    private void startGameLoop(GraphicsContext gc) {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                deltaTime = Math.min(deltaTime, 0.05);

                gameManager.updateGame(deltaTime);
                gameManager.render(gc);

                // Update UI panel
                updateUIPanel();

                // Theo dõi state changes
                GameManager.GameState state = gameManager.getGameState();
                if (lastState != state) {
                    System.out.println("==========================================");
                    System.out.println("STATE CHANGE: " + lastState + " → " + state);
                    System.out.println("==========================================");

                    if (state == GameManager.GameState.PAUSED) {
                        if (pauseOverlay != null) {
                            pauseOverlay.setVisible(true);
                            pauseOverlay.setMouseTransparent(false);
                        }
                    } else if (state == GameManager.GameState.PLAYING) {
                        if (btnPause != null) {
                            btnPause.setVisible(true);
                        }
                        gameOverShown = false; // Reset flag khi chơi lại
                        if (pauseOverlay != null) {
                            pauseOverlay.setVisible(false);
                            pauseOverlay.setMouseTransparent(true);
                        }
                        gameCanvas.requestFocus();
                    } else if (state == GameManager.GameState.GAME_OVER) {
                        handleGameOver();
                    }
                    lastState = state;
                }
            }
        };
        gameLoop.start();
        System.out.println("Game loop started");
    }

    private void updateUIPanel() {
        // Animate score khi thay đổi
        int currentScore = gameManager.getScore();
        if (lblScore != null && currentScore != lastScore) {
            // Tính điểm vừa được cộng
            int pointsAdded = currentScore - lastScore;

            if (pointsAdded > 0) {
                // Hiển thị +XX bay lên
                if (scoreContainer != null) {
                    // Kiểm tra chính xác xem có phải điểm Streak không
                    boolean isStreak = (pointsAdded == gameManager.getLastStreakBonus() &&
                            gameManager.getLastStreakBonus() > 0);

                    ScoreAnimation.showFloatingScore(
                            scoreContainer,
                            80,
                            15,
                            pointsAdded,
                            isStreak  // true = cam (streak), false = xanh lá (bình thường)
                    );
                }

                // 1. ĐIỀU CHỈNH FONT SIZE TRƯỚC
                adjustScoreFontSize(currentScore);

                // 2. SAU ĐÓ MỚI ANIMATION (để animateScoreCount dùng font mới)
                ScoreAnimation.animateScoreCount(lblScore, lastScore, currentScore);

                // 3. FLASH SAU CÙNG (flashLabel sẽ lưu style mới)
                ScoreAnimation.flashLabel(lblScore);

            } else {
                // Nếu không có animation (ví dụ reset game), update trực tiếp
                lblScore.setText(String.valueOf(currentScore));
            }

            lastScore = currentScore;
        }

        if (lblLives != null) {
            lblLives.setText(String.valueOf(gameManager.getLives()));
        }

        if (lblLevel != null) {
            lblLevel.setText(String.valueOf(gameManager.getLevel()));
        }

        // Update balls counter
        int ballCount = gameManager.getBalls().size();
        if (ballsContainer != null) {
            if (ballCount > 1) {
                ballsContainer.setVisible(true);
                ballsContainer.setManaged(true);
                if (lblBalls != null) {
                    lblBalls.setText(String.valueOf(ballCount));
                }
            } else {
                ballsContainer.setVisible(false);
                ballsContainer.setManaged(false);
            }
        }

        // Update active power-ups
        updatePowerUpsDisplay();
    }

    /**
     * Cập nhật hiển thị power-ups đang active
     */
    private void updatePowerUpsDisplay() {
        if (powerUpsContainer == null) return;

        // Clear old power-ups (except the "no power-ups" label)
        powerUpsContainer.getChildren().clear();

        var activePowerUps = gameManager.getActivePowerUps();

        if (activePowerUps.isEmpty()) {
            if (lblNoPowerUps != null) {
                lblNoPowerUps.setText("No active power-ups");
                lblNoPowerUps.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b93a5; -fx-font-style: italic;");
                powerUpsContainer.getChildren().add(lblNoPowerUps);
            }
        } else {
            for (PowerUp powerUp : activePowerUps) {
                if (powerUp.getTimeRemaining() > 0) {
                    Label powerUpLabel = new Label(
                            powerUp.getDisplaySymbol() + " " +
                                    String.format("%.1fs", powerUp.getTimeRemaining())
                    );
                    powerUpLabel.setStyle(
                            "-fx-font-size: 13px; " +
                                    "-fx-text-fill: #fbbf24; " +
                                    "-fx-background-color: rgba(251, 191, 36, 0.1); " +
                                    "-fx-padding: 4 8; " +
                                    "-fx-background-radius: 4;"
                    );
                    powerUpsContainer.getChildren().add(powerUpLabel);
                }
            }
        }
    }

    /**
     * Xử lý khi game over
     */
    private void handleGameOver() {
        if (gameOverShown) {
            System.out.println("Game Over already shown, skipping...");
            return;
        }

        if (btnPause != null) {
            btnPause.setVisible(false);
        }

        gameOverShown = true;
        System.out.println("GAME OVER DETECTED!");

        // Dừng game loop
        if (gameLoop != null) {
            gameLoop.stop();
            System.out.println("Game loop stopped");
        }

        // Reset input
        leftKeyDown = false;
        rightKeyDown = false;
        gameManager.setMovingLeft(false);
        gameManager.setMovingRight(false);

        // Delay nhỏ để đảm bảo render cuối cùng hoàn tất
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(100); // Delay 100ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Kiểm tra nếu đạt high score → hiển thị popup nhập tên trước
            if (HighScoreManager.getInstance().isHighScore(gameManager.getScore())) {
                showHighScoreInputFirst();
            } else {
                // Không đạt high score → hiển thị Game Over bình thường
                showGameOverOverlay();
            }
        });
    }

    /**
     * Hiển thị Game Over overlay - LOAD TỪ FXML
     */
    /**
     * Hiển thị Game Over overlay - LOAD TỪ FXML
     */
    /**
     * Hiển thị Game Over overlay - LOAD TỪ FXML
     */
    private void showGameOverOverlay() {
        System.out.println("====================================");
        System.out.println("showGameOverOverlay() CALLED");
        System.out.println("====================================");

        try {
            Stage stage = (Stage) gameCanvas.getScene().getWindow();
            if (stage == null) {
                System.err.println("ERROR: Stage is NULL!");
                return;
            }

            // === LOAD FXML ===
            System.out.println("Loading GameOver.fxml...");

            FXMLLoader loader = new FXMLLoader();
            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/GameOver.fxml");

            Parent overlay = null;

            if (resourceUrl != null) {
                System.out.println("Loading from resources: " + resourceUrl);
                loader.setLocation(resourceUrl);
                overlay = loader.load();
            } else {
                System.out.println("Resource not found, trying file path...");
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/GameOver.fxml");

                if (fxmlFile.exists()) {
                    System.out.println("Loading from file: " + fxmlFile.getAbsolutePath());
                    loader.setLocation(fxmlFile.toURI().toURL());
                    overlay = loader.load();
                } else {
                    System.err.println("GameOver.fxml NOT FOUND!");
                    overlay = createSimpleGameOverOverlay();
                }
            }

            if (overlay == null) {
                System.err.println("Failed to load overlay");
                overlay = createSimpleGameOverOverlay();
            } else {
                System.out.println("GameOver.fxml loaded successfully!");

                // Lấy controller và set stats
                GameOverController ctrl = loader.getController();
                if (ctrl != null) {
                    ctrl.setStats(gameManager.getScore(), gameManager.getLevel());
                    System.out.println("Stats set: Score=" + gameManager.getScore() + ", Level=" + gameManager.getLevel());
                } else {
                    System.err.println(" GameOverController is NULL!");
                }
            }

            // THAY THẾ TOÀN BỘ SCENE - KHÔNG DÙNG STACKPANE
            Scene newScene = new Scene(overlay, 800, 600);

            // Load stylesheet từ file hoặc resource
            try {
                java.net.URL cssUrl = getClass().getResource("/ui/css/style.css");
                if (cssUrl != null) {
                    newScene.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    java.io.File cssFile = new java.io.File("src/arkanoid/ui/css/style.css");
                    if (cssFile.exists()) {
                        newScene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
                    }
                }
            } catch (Exception e) {
                System.err.println(" Could not load stylesheet");
            }

            stage.setScene(newScene);
            System.out.println(" Game Over scene set successfully!");

        } catch (Exception ex) {
            System.err.println(" EXCEPTION in showGameOverOverlay:");
            ex.printStackTrace();
        }
    }

    /**
     * Tạo overlay đơn giản nếu không load được FXML
     */
    private Parent createSimpleGameOverOverlay() {
        System.out.println("🔧 Creating simple fallback overlay...");

        VBox simpleOverlay = new VBox(20);
        simpleOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); -fx-alignment: center; -fx-padding: 50;");
        simpleOverlay.setPrefSize(800, 600);

        Label title = new Label("GAME OVER");
        title.setStyle("-fx-font-size: 72px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

        Label scoreLabel = new Label("Final Score: " + gameManager.getScore());
        scoreLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        Label levelLabel = new Label("Level: " + gameManager.getLevel());
        levelLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        Button btnRestart = new Button("Restart");
        btnRestart.setStyle("-fx-font-size: 18px; -fx-padding: 10 30;");
        btnRestart.setOnAction(e -> {
            gameManager.startGame();
            try {
                Stage stage = (Stage) gameCanvas.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/fxml/GameView.fxml"));
                Parent root = loader.load();
                stage.setScene(new Scene(root, 1000, 600));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        simpleOverlay.getChildren().addAll(title, scoreLabel, levelLabel, btnRestart);

        return simpleOverlay;
    }

    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        leftKeyDown = false;
        rightKeyDown = false;
        gameManager.setMovingLeft(false);
        gameManager.setMovingRight(false);
    }

    private void stopLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }

    public static void stopGameLoopIfAny() {
        if (lastInstance != null) lastInstance.stopLoop();
    }

    /**
     * Tự động điều chỉnh font size để số vừa khít trong không gian 89px
     */
    private void adjustScoreFontSize(int score) {
        if (lblScore == null) return;

        String scoreText = String.valueOf(score);
        int digits = scoreText.length();
        int fontSize;

        // Tính toán font size dựa trên số chữ số
        // Width ~89px, mỗi chữ số chiếm khoảng 15-18px tùy font size
        if (digits <= 4) {
            fontSize = 22; // 0-9999: font to nhất
        } else if (digits == 5) {
            fontSize = 18; // 10000-99999
        } else if (digits == 6) {
            fontSize = 15; // 100000-999999
        } else if (digits == 7) {
            fontSize = 13; // 1000000-9999999
        } else {
            fontSize = 11; // 10000000+: font nhỏ nhất
        }

        // Chỉ update style, KHÔNG đổi text (để animation tự xử lý)
        lblScore.setStyle(String.format(
                "-fx-font-size: %dpx; -fx-font-weight: bold; -fx-text-fill: white;",
                fontSize
        ));
    }

    /**
     * Hiển thị popup nhập tên HIGH SCORE TRƯỚC, sau đó mới hiển thị Game Over
     */
    private void showHighScoreInputFirst() {
        System.out.println("🎉 NEW HIGH SCORE! Showing input popup first...");

        try {
            Stage stage = (Stage) gameCanvas.getScene().getWindow();

            // Tạo overlay tối để che game canvas
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
            overlay.setPrefSize(800, 600);

            // Load popup nhập tên
            FXMLLoader loader = new FXMLLoader();
            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/NewHighScore.fxml");

            if (resourceUrl != null) {
                loader.setLocation(resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/NewHighScore.fxml");
                loader.setLocation(fxmlFile.toURI().toURL());
            }

            Parent popup = loader.load();
            NewHighScoreController controller = loader.getController();

            // Set stats
            controller.setStats(gameManager.getScore(), gameManager.getLevel());

            //  QUAN TRỌNG: Set callback để hiển thị Game Over SAU KHI đóng popup
            controller.setOnClose(() -> {
                System.out.println(" High score saved! Now showing Game Over screen...");
                showGameOverOverlay();
            });

            overlay.getChildren().add(popup);

            // Hiển thị overlay trên canvas hiện tại
            Scene currentScene = stage.getScene();
            if (currentScene.getRoot() instanceof StackPane) {
                ((StackPane) currentScene.getRoot()).getChildren().add(overlay);
            } else {
                // Wrap root vào StackPane nếu chưa có
                Parent oldRoot = currentScene.getRoot();
                StackPane newRoot = new StackPane(oldRoot, overlay);
                currentScene.setRoot(newRoot);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(" Error showing high score input, falling back to Game Over");
            showGameOverOverlay();
        }
    }
}