package arkanoid.ui.controller;

import arkanoid.core.GameManager;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GameController {

    @FXML
    private Canvas gameCanvas;

    @FXML
    private Button btnPause;

    @FXML
    private javafx.scene.layout.StackPane pauseOverlay;

    @FXML
    private PauseOverlayController pauseOverlayController;
    private static GameController lastInstance;
    private GameManager.GameState lastState = null;

    private GameManager gameManager;
    private AnimationTimer gameLoop;

    // Track key states
    private boolean leftKeyDown = false;
    private boolean rightKeyDown = false;

    // Flag để tránh hiển thị Game Over nhiều lần
    private boolean gameOverShown = false;

    @FXML
    private void initialize() {
        lastInstance = this;
        System.out.println("🎮 GameController initialize called");
        System.out.println("📐 Canvas size: " + gameCanvas.getWidth() + "x" + gameCanvas.getHeight());

        // Ẩn overlay lúc đầu
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
            pauseOverlay.setMouseTransparent(true);
        }

        gameManager = GameManager.getInstance();

        if (gameManager.hasSavedGame()) {
            System.out.println("📄 Continuing from saved game...");
        } else {
            System.out.println("🆕 Starting new game...");
            gameManager.setGameState(GameManager.GameState.MENU);
            gameManager.startGame();
        }

        System.out.println("✅ Game state after init: " + gameManager.getGameState());

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gameCanvas.setFocusTraversable(true);

        // Bắt đầu game loop
        startGameLoop(gc);

        // Setup input handlers
        gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                System.out.println("🎯 Scene ready, setting up input handlers");

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
            System.out.println("⌨️ Focus requested");
        });
    }

    private void handleKeyPress(KeyCode code, boolean isPressed) {
        if (code == KeyCode.A || code == KeyCode.LEFT) {
            if (isPressed && !leftKeyDown) {
                leftKeyDown = true;
                gameManager.setMovingLeft(true);
            } else if (!isPressed && leftKeyDown) {
                leftKeyDown = false;
                gameManager.setMovingLeft(false);
            }
        }

        if (code == KeyCode.D || code == KeyCode.RIGHT) {
            if (isPressed && !rightKeyDown) {
                rightKeyDown = true;
                gameManager.setMovingRight(true);
            } else if (!isPressed && rightKeyDown) {
                rightKeyDown = false;
                gameManager.setMovingRight(false);
            }
        }

        if (isPressed) {
            if (code == KeyCode.P || code == KeyCode.ESCAPE) {
                gameManager.togglePause();
            }
            if (code == KeyCode.SPACE && gameManager.getGameState() == GameManager.GameState.MENU) {
                gameManager.startGame();
            }
            if (code == KeyCode.R && gameManager.getGameState() == GameManager.GameState.GAME_OVER) {
                gameManager.startGame();
            }
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

                // Theo dõi state changes
                GameManager.GameState state = gameManager.getGameState();
                if (lastState != state) {
                    System.out.println("==========================================");
                    System.out.println("🔄 STATE CHANGE: " + lastState + " → " + state);
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
        System.out.println("✅ Game loop started");
    }

    /**
     * Xử lý khi game over
     */
    private void handleGameOver() {
        if (gameOverShown) {
            System.out.println("⚠️ Game Over already shown, skipping...");
            return;
        }

        if (btnPause != null) {
            btnPause.setVisible(false);
        }

        gameOverShown = true;
        System.out.println("💀 GAME OVER DETECTED!");

        // Dừng game loop
        if (gameLoop != null) {
            gameLoop.stop();
            System.out.println("⏸️ Game loop stopped");
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
            showGameOverOverlay();
        });
    }

    /**
     * Hiển thị Game Over overlay - LOAD TỪ FXML
     */
    private void showGameOverOverlay() {
        System.out.println("====================================");
        System.out.println("🎯 showGameOverOverlay() CALLED");
        System.out.println("====================================");

        try {
            Scene scene = gameCanvas.getScene();
            if (scene == null) {
                System.err.println("❌ ERROR: Scene is NULL!");
                return;
            }
            System.out.println("✅ Scene found: " + scene);

            Parent currentRoot = scene.getRoot();
            System.out.println("✅ Current root: " + currentRoot.getClass().getName());

            // Tạo container
            StackPane container = new StackPane();
            container.getChildren().add(currentRoot);
            scene.setRoot(container);
            System.out.println("✅ Container created and set as root");

            // === LOAD FXML ===
            System.out.println("📂 Attempting to load GameOver.fxml...");

            FXMLLoader loader = new FXMLLoader();
            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/GameOver.fxml");

            Parent overlay = null;

            if (resourceUrl != null) {
                System.out.println("✅ Loading from resources: " + resourceUrl);
                loader.setLocation(resourceUrl);
                overlay = loader.load();
            } else {
                System.out.println("⚠️ Resource not found, trying file path...");
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/GameOver.fxml");

                if (fxmlFile.exists()) {
                    System.out.println("✅ Loading from file: " + fxmlFile.getAbsolutePath());
                    loader.setLocation(fxmlFile.toURI().toURL());
                    overlay = loader.load();
                } else {
                    System.err.println("❌ GameOver.fxml NOT FOUND in file system!");
                    System.err.println("   Tried: " + fxmlFile.getAbsolutePath());

                    // Fallback: Tạo overlay đơn giản
                    overlay = createSimpleGameOverOverlay();
                }
            }

            if (overlay == null) {
                System.err.println("❌ Failed to load overlay, creating simple one...");
                overlay = createSimpleGameOverOverlay();
            } else {
                System.out.println("✅ GameOver.fxml loaded successfully!");

                // Lấy controller và set stats
                GameOverController ctrl = loader.getController();
                if (ctrl != null) {
                    ctrl.setStats(gameManager.getScore(), gameManager.getLevel());
                    System.out.println("✅ Stats set: Score=" + gameManager.getScore() + ", Level=" + gameManager.getLevel());
                } else {
                    System.err.println("⚠️ GameOverController is NULL!");
                }
            }

            // Thêm overlay vào container
            container.getChildren().add(overlay);
            System.out.println("✅ Overlay added to container");
            System.out.println("📊 Container has " + container.getChildren().size() + " children");

            // Force refresh
            container.requestLayout();

        } catch (Exception ex) {
            System.err.println("❌ EXCEPTION in showGameOverOverlay:");
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
                stage.setScene(new Scene(root, 800, 600));
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
}