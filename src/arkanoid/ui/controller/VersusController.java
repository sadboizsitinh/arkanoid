package arkanoid.ui.controller;

import arkanoid.core.VersusGameManager;
import arkanoid.utils.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class VersusController {

    @FXML
    private AnchorPane paneP1;

    @FXML
    private AnchorPane paneP2;

    @FXML
    private Label lblScore1;

    @FXML
    private Label lblLives1;

    @FXML
    private Label lblScore2;

    @FXML
    private Label lblLives2;

    private Canvas canvasP1;
    private Canvas canvasP2;

    private VersusGameManager gameManager;
    private AnimationTimer gameLoop;

    private static VersusController lastInstance;

    private double lastP1RotateTime = 0;
    private double lastP2RotateTime = 0;
    private static final double ROTATE_COOLDOWN = 0.15;

    @FXML
    private void initialize() {
        lastInstance = this;

        System.out.println("✅ VersusController initialized");

        canvasP1 = new Canvas(640, 600);
        canvasP2 = new Canvas(640, 600);

        // Make canvas focusable
        canvasP1.setFocusTraversable(true);
        canvasP2.setFocusTraversable(true);

        AnchorPane.setTopAnchor(canvasP1, 0.0);
        AnchorPane.setBottomAnchor(canvasP1, 0.0);
        AnchorPane.setLeftAnchor(canvasP1, 0.0);
        AnchorPane.setRightAnchor(canvasP1, 0.0);

        AnchorPane.setTopAnchor(canvasP2, 0.0);
        AnchorPane.setBottomAnchor(canvasP2, 0.0);
        AnchorPane.setLeftAnchor(canvasP2, 0.0);
        AnchorPane.setRightAnchor(canvasP2, 0.0);

        paneP1.getChildren().add(canvasP1);
        paneP2.getChildren().add(canvasP2);

        gameManager = VersusGameManager.getInstance();
        gameManager.startNewGame();

        GraphicsContext gc1 = canvasP1.getGraphicsContext2D();
        GraphicsContext gc2 = canvasP2.getGraphicsContext2D();

        // ✅ FIX: Setup input handlers NGAY trong Platform.runLater
        javafx.application.Platform.runLater(() -> {
            try {
                Stage stage = (Stage) paneP1.getScene().getWindow();
                if (stage != null) {
                    stage.setWidth(1320);
                    stage.setHeight(740);
                    stage.centerOnScreen();
                    stage.setResizable(false);
                    System.out.println("✅ Window resized to 1320x740");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Cannot resize window: " + e.getMessage());
            }

            // ✅ FIX: Setup input AFTER scene is ready
            setupInputHandlers();

            // Request focus cho canvas
            canvasP1.requestFocus();
            System.out.println("✅ Canvas focus requested");
        });

        startGameLoop(gc1, gc2);
    }

    private void setupInputHandlers() {
        Scene scene = paneP1.getScene();

        if (scene != null) {
            System.out.println("✅ Scene available, setting up handlers immediately");
            attachKeyHandlers(scene);
        } else {
            System.out.println("⚠️ Scene not ready, waiting for sceneProperty");
            // Fallback: Wait for scene to be ready
            paneP1.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    System.out.println("✅ Scene ready via listener, setting up handlers");
                    attachKeyHandlers(newScene);
                }
            });
        }
    }

    private void attachKeyHandlers(Scene scene) {
        // ✅ FIX: Remove old handlers first to prevent duplicates
        scene.setOnKeyPressed(null);
        scene.setOnKeyReleased(null);

        // ✅ FIX: Use setOnKeyPressed/Released instead of addEventFilter
        scene.setOnKeyPressed(event -> {
            System.out.println("🎮 KEY PRESSED: " + event.getCode());
            handleKeyPress(event.getCode(), true);
            event.consume();
        });

        scene.setOnKeyReleased(event -> {
            System.out.println("🎮 KEY RELEASED: " + event.getCode());
            handleKeyPress(event.getCode(), false);
            event.consume();
        });

        // ✅ FIX: Ensure focus stays on scene
        scene.getWindow().focusedProperty().addListener((o, oldVal, newVal) -> {
            if (newVal) {
                System.out.println("🔍 Window gained focus, requesting canvas focus");
                canvasP1.requestFocus();
            }
        });

        // ✅ FIX: Add mouse click to regain focus if lost
        scene.setOnMouseClicked(event -> {
            canvasP1.requestFocus();
            System.out.println("🖱️ Mouse clicked, focus regained");
        });

        System.out.println("✅ Input handlers attached successfully");
    }

    private void handleKeyPress(KeyCode code, boolean isPressed) {
        VersusGameManager.VersusState state = gameManager.getGameState();

        // Debug log
        System.out.println("📋 handleKeyPress: " + code + " | pressed=" + isPressed + " | state=" + state);

        // Handle PAUSE and ESC in any state
        if (code == KeyCode.P && isPressed) {
            gameManager.togglePause();
            System.out.println("⏸️ Toggle pause - new state: " + gameManager.getGameState());
            return;
        }

        if (code == KeyCode.ESCAPE && isPressed) {
            goToMainMenu();
            return;
        }

        // Only process game controls when PLAYING
        if (state != VersusGameManager.VersusState.PLAYING) {
            return;
        }

        // === PLAYER 1 CONTROLS (A, D, W) ===
        if (code == KeyCode.A) {
            System.out.println("👈 P1 LEFT: " + isPressed);
            gameManager.setPlayer1Left(isPressed);

            if (isPressed) {
                boolean ballStuck = gameManager.getBalls1().stream()
                        .anyMatch(ball -> ball.isStuckToPaddle());
                if (ballStuck) {
                    double currentTime = System.currentTimeMillis() / 1000.0;
                    if (currentTime - lastP1RotateTime > ROTATE_COOLDOWN) {
                        gameManager.rotatePlayer1Ball(false);
                        lastP1RotateTime = currentTime;
                        System.out.println("🔄 P1 rotate left");
                    }
                }
            }
        }

        if (code == KeyCode.D) {
            System.out.println("👉 P1 RIGHT: " + isPressed);
            gameManager.setPlayer1Right(isPressed);

            if (isPressed) {
                boolean ballStuck = gameManager.getBalls1().stream()
                        .anyMatch(ball -> ball.isStuckToPaddle());
                if (ballStuck) {
                    double currentTime = System.currentTimeMillis() / 1000.0;
                    if (currentTime - lastP1RotateTime > ROTATE_COOLDOWN) {
                        gameManager.rotatePlayer1Ball(true);
                        lastP1RotateTime = currentTime;
                        System.out.println("🔄 P1 rotate right");
                    }
                }
            }
        }

        if (code == KeyCode.W && isPressed) {
            System.out.println("🔥 P1 FIRE");
            gameManager.firePlayer1Ball();
        }

        // === PLAYER 2 CONTROLS (LEFT, RIGHT, UP) ===
        if (code == KeyCode.LEFT) {
            System.out.println("👈 P2 LEFT: " + isPressed);
            gameManager.setPlayer2Left(isPressed);

            if (isPressed) {
                boolean ballStuck = gameManager.getBalls2().stream()
                        .anyMatch(ball -> ball.isStuckToPaddle());
                if (ballStuck) {
                    double currentTime = System.currentTimeMillis() / 1000.0;
                    if (currentTime - lastP2RotateTime > ROTATE_COOLDOWN) {
                        gameManager.rotatePlayer2Ball(false);
                        lastP2RotateTime = currentTime;
                        System.out.println("🔄 P2 rotate left");
                    }
                }
            }
        }

        if (code == KeyCode.RIGHT) {
            System.out.println("👉 P2 RIGHT: " + isPressed);
            gameManager.setPlayer2Right(isPressed);

            if (isPressed) {
                boolean ballStuck = gameManager.getBalls2().stream()
                        .anyMatch(ball -> ball.isStuckToPaddle());
                if (ballStuck) {
                    double currentTime = System.currentTimeMillis() / 1000.0;
                    if (currentTime - lastP2RotateTime > ROTATE_COOLDOWN) {
                        gameManager.rotatePlayer2Ball(true);
                        lastP2RotateTime = currentTime;
                        System.out.println("🔄 P2 rotate right");
                    }
                }
            }
        }

        if (code == KeyCode.UP && isPressed) {
            System.out.println("🔥 P2 FIRE");
            gameManager.firePlayer2Ball();
        }
    }

    private void startGameLoop(GraphicsContext gc1, GraphicsContext gc2) {
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

                gameManager.update(deltaTime);

                gameManager.renderPlayer1(gc1);
                gameManager.renderPlayer2(gc2);

                updateUI();

                if (gameManager.getGameState() == VersusGameManager.VersusState.GAME_OVER) {
                    handleGameOver();
                }
            }
        };
        gameLoop.start();
        System.out.println("✅ Game loop started");
    }

    private void updateUI() {
        if (lblScore1 != null) {
            lblScore1.setText(String.valueOf(gameManager.getScore1()));
        }
        if (lblLives1 != null) {
            lblLives1.setText(String.valueOf(gameManager.getLives1()));
        }
        if (lblScore2 != null) {
            lblScore2.setText(String.valueOf(gameManager.getScore2()));
        }
        if (lblLives2 != null) {
            lblLives2.setText(String.valueOf(gameManager.getLives2()));
        }
    }

    private void handleGameOver() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        javafx.application.Platform.runLater(() -> {
            try {
                Stage stage = (Stage) canvasP1.getScene().getWindow();

                FXMLLoader loader = new FXMLLoader();
                java.net.URL resourceUrl = getClass().getResource("/ui/fxml/VersusGameOver.fxml");

                if (resourceUrl != null) {
                    loader.setLocation(resourceUrl);
                } else {
                    java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/VersusGameOver.fxml");
                    loader.setLocation(fxmlFile.toURI().toURL());
                }

                Parent root = loader.load();

                VersusGameOverController controller = loader.getController();
                controller.setStats(
                        gameManager.getScore1(),
                        gameManager.getScore2(),
                        gameManager.getWinner()
                );

                stage.setScene(new Scene(root, 1296, 800));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void goToMainMenu() {
        try {
            if (gameLoop != null) {
                gameLoop.stop();
            }

            Stage stage = (Stage) canvasP1.getScene().getWindow();
            java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/Main.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public static void stopGameLoopIfAny() {
        if (lastInstance != null) {
            lastInstance.cleanup();
        }
    }
}