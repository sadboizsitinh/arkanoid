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
import javafx.scene.input.KeyCode;
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

    // Track key states để tránh xử lý trùng lặp
    private boolean leftKeyDown = false;
    private boolean rightKeyDown = false;

    @FXML
    private void initialize() {
        lastInstance = this;
        System.out.println("GameController initialize called");
        System.out.println("Canvas size: " + gameCanvas.getWidth() + "x" + gameCanvas.getHeight());

        // Ẩn overlay lúc đầu
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
            pauseOverlay.setMouseTransparent(true);
        }

        gameManager = GameManager.getInstance();

        if (gameManager.hasSavedGame()) {
            System.out.println("🔄 Continuing from saved game...");
            // Không gọi startGame() - game state đã được set sẵn bởi continueGame()
        } else {
            System.out.println("🆕 Starting new game...");
            // Chỉ start game mới khi KHÔNG có saved game
            gameManager.setGameState(GameManager.GameState.MENU);
            gameManager.startGame(); // Chuyển sang PLAYING
        }

        System.out.println("Game state after init: " + gameManager.getGameState());

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // Canvas phải có focus để nhận keyboard events
        gameCanvas.setFocusTraversable(true);

        // Bắt đầu vòng lặp game TRƯỚC KHI thiết lập input
        startGameLoop(gc);

        // Xử lý input - đợi scene sẵn sàng
        gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                System.out.println("Scene ready, setting up input handlers");

                // Khi nhấn phím
                newScene.setOnKeyPressed(event -> {
                    handleKeyPress(event.getCode(), true);
                    event.consume();
                });

                // Khi thả phím
                newScene.setOnKeyReleased(event -> {
                    handleKeyPress(event.getCode(), false);
                    event.consume();
                });

                // Request focus
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

        // Request focus ngay lập tức nếu có thể
        javafx.application.Platform.runLater(() -> {
            gameCanvas.requestFocus();
            System.out.println("Focus requested");
        });
    }

    /**
     * Xử lý sự kiện phím bấm - với tracking để tránh duplicate events
     */
    private void handleKeyPress(KeyCode code, boolean isPressed) {
        // Phím di chuyển - CHỈ XỬ LÝ KHI TRẠNG THÁI THAY ĐỔI
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

        // Các phím đặc biệt - chỉ khi nhấn (không phải thả)
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
                // Tính delta time chính xác hơn
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0; // Convert to seconds
                lastUpdate = now;

                // Cap delta time để tránh jump lớn
                deltaTime = Math.min(deltaTime, 0.05);

                gameManager.updateGame(deltaTime);
                gameManager.render(gc);

                // Theo dõi trạng thái game
                GameManager.GameState state = gameManager.getGameState();
                if (lastState != state) {
                    System.out.println("State changed: " + lastState + " -> " + state);

                    if (state == GameManager.GameState.PAUSED) {
                        if (pauseOverlay != null) {
                            pauseOverlay.setVisible(true);
                            pauseOverlay.setMouseTransparent(false);
                        }
                    } else if (state == GameManager.GameState.PLAYING) {
                        if (pauseOverlay != null) {
                            pauseOverlay.setVisible(false);
                            pauseOverlay.setMouseTransparent(true);
                        }
                        // Request focus lại khi resume
                        gameCanvas.requestFocus();
                    } else if (state == GameManager.GameState.GAME_OVER) {
                        // DỪNG GAME LOOP TRƯỚC KHI CHUYỂN SCENE
                        gameLoop.stop();

                        // Reset input flags
                        leftKeyDown = false;
                        rightKeyDown = false;
                        gameManager.setMovingLeft(false);
                        gameManager.setMovingRight(false);

                        javafx.application.Platform.runLater(() -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/fxml/GameOver.fxml"));
                                Parent root = loader.load();
                                GameOverController ctrl = loader.getController();
                                ctrl.setStats(gameManager.getScore(), gameManager.getLevel());

                                stopLoop();
                                Stage stage = (Stage) gameCanvas.getScene().getWindow();
                                stage.setScene(new Scene(root, 800, 600));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    lastState = state;
                }
            }
        };
        gameLoop.start();
        System.out.println("Game loop started");
    }

    /**
     * Cleanup khi controller bị destroy
     */
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