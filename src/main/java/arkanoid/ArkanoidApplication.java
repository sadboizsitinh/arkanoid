package arkanoid;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main JavaFX Application class
 * Handles window creation, input, and game loop
 */
public class ArkanoidApplication extends Application {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private GameManager gameManager;
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private long lastTime;

    @Override
    public void start(Stage primaryStage) {
        initializeGame();
        setupUI(primaryStage);
        startGameLoop();
    }

    private void initializeGame() {
        gameManager = GameManager.getInstance();
        lastTime = System.nanoTime();
    }

    private void setupUI(Stage primaryStage) {
        primaryStage.setTitle("Arkanoid Game - JavaFX Version");

        // Create canvas
        canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // Setup scene
        VBox root = new VBox(canvas);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Handle keyboard input
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Request focus for keyboard input
        canvas.requestFocus();
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentTime) {
                double deltaTime = (currentTime - lastTime) / 1_000_000_000.0; // Convert to seconds
                lastTime = currentTime;

                // Cap delta time to prevent large jumps
                deltaTime = Math.min(deltaTime, 1.0 / 60.0);

                update(deltaTime);
                render();
            }
        };
        gameLoop.start();
    }

    private void update(double deltaTime) {
        gameManager.updateGame(deltaTime);
    }

    private void render() {
        gameManager.render(gc);
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        switch (code) {
            case SPACE:
                if (gameManager.getGameState() == GameManager.GameState.MENU) {
                    gameManager.startGame();
                }
                break;

            case R:
                if (gameManager.getGameState() == GameManager.GameState.GAME_OVER) {
                    gameManager.startGame();
                }
                break;

            case ESCAPE:
                System.exit(0);
                break;

            default:
                gameManager.handleKeyPress(code.toString());
                break;
        }
    }


    private void handleKeyReleased(KeyEvent event) {
        // Handle key release if needed for smoother movement
    }

    @Override
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
