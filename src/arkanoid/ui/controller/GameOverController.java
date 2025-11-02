package arkanoid.ui.controller;

import arkanoid.core.GameManager;
import arkanoid.core.GameStatePersistence;
import arkanoid.core.HighScoreManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameOverController {
    @FXML private Label lblScore, lblLevel;
    @FXML private Button btnRestart, btnBack;

    private int finalScore;
    private int finalLevel;

    public void setStats(int score, int level) {
        this.finalScore = score;
        this.finalLevel = level;

        System.out.println("============================================");
        System.out.println("   GameOver.setStats() called");
        System.out.println("   Score: " + score);
        System.out.println("   Level: " + level);
        System.out.println("============================================");

        if (lblScore != null) {
            // Fade in + scale animation cho score
            lblScore.setOpacity(0);
            lblScore.setScaleX(0.5);
            lblScore.setScaleY(0.5);

            PauseTransition delay = new PauseTransition(Duration.seconds(0.3));
            delay.setOnFinished(e -> {
                // Count up animation
                animateScoreCountUp(0, score, 1.5);

                // Fade + Scale in
                FadeTransition fade = new FadeTransition(Duration.seconds(0.8), lblScore);
                fade.setFromValue(0);
                fade.setToValue(1);

                ScaleTransition scale = new ScaleTransition(Duration.seconds(0.8), lblScore);
                scale.setFromX(0.5);
                scale.setFromY(0.5);
                scale.setToX(1.0);
                scale.setToY(1.0);

                ParallelTransition parallel = new ParallelTransition(fade, scale);
                parallel.play();
            });
            delay.play();
        }

        if (lblLevel != null) {
            // Fade in + rotate animation cho level
            lblLevel.setOpacity(0);
            lblLevel.setRotate(-180);

            PauseTransition delay = new PauseTransition(Duration.seconds(0.8));
            delay.setOnFinished(e -> {
                lblLevel.setText(String.valueOf(level));

                FadeTransition fade = new FadeTransition(Duration.seconds(0.6), lblLevel);
                fade.setFromValue(0);
                fade.setToValue(1);

                RotateTransition rotate = new RotateTransition(Duration.seconds(0.6), lblLevel);
                rotate.setFromAngle(-180);
                rotate.setToAngle(0);

                ParallelTransition parallel = new ParallelTransition(fade, rotate);
                parallel.play();
            });
            delay.play();
        }

    }

    /**
     * Animation đếm số từ 0 lên finalScore
     */
    private void animateScoreCountUp(int start, int end, double durationSeconds) {
        Timeline timeline = new Timeline();
        final int steps = 50; // Số bước animation
        final long stepDuration = (long) (durationSeconds * 1000 / steps);

        for (int i = 0; i <= steps; i++) {
            final int value = start + (int) ((end - start) * i / (double) steps);
            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(i * stepDuration),
                    e -> lblScore.setText(String.valueOf(value))
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }

    @FXML
    private void initialize() {
        GameStatePersistence.deleteSaveFile();

        // Animation entrance cho buttons với stagger effect
        javafx.application.Platform.runLater(() -> {
            if (btnRestart != null) {
                animateButtonEntrance(btnRestart, 1.8);
            }
            if (btnBack != null) {
                animateButtonEntrance(btnBack, 2.0);
            }
        });

        if (btnRestart != null) {
            btnRestart.setOnAction(e -> {
                GameStatePersistence.deleteSaveFile();
                GameManager.getInstance().startGame();
                try {
                    GameController.stopGameLoopIfAny();
                    GameManager.getInstance().clearSavedGame();
                    GameManager.getInstance().startGame();

                    Stage stage = (Stage) btnRestart.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader();

                    java.net.URL resourceUrl = getClass().getResource("/ui/fxml/GameView.fxml");
                    if (resourceUrl != null) {
                        loader.setLocation(resourceUrl);
                    } else {
                        java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/GameView.fxml");
                        loader.setLocation(fxmlFile.toURI().toURL());
                    }

                    Parent root = loader.load();
                    stage.setScene(new Scene(root, 1000, 600));

                    System.out.println("Game restarted successfully!");

                } catch (Exception ex) {
                    System.err.println("Error restarting game:");
                    ex.printStackTrace();
                }
            });
        }

        if (btnBack != null) {
            btnBack.setOnAction(e -> goToMainMenu());
        }
    }

    /**
     * Animation đẹp hơn cho button entrance
     */
    private void animateButtonEntrance(Button button, double delay) {
        button.setOpacity(0);
        button.setScaleX(0.8);
        button.setScaleY(0.8);
        button.setTranslateY(20);

        PauseTransition pause = new PauseTransition(Duration.seconds(delay));
        pause.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.seconds(0.4), button);
            fade.setFromValue(0);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(0.4), button);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1.0);
            scale.setToY(1.0);

            TranslateTransition translate = new TranslateTransition(Duration.seconds(0.4), button);
            translate.setFromY(20);
            translate.setToY(0);

            ParallelTransition parallel = new ParallelTransition(fade, scale, translate);
            parallel.play();

            // Thêm bounce effect nhẹ
            parallel.setOnFinished(ev -> {
                ScaleTransition bounce = new ScaleTransition(Duration.seconds(0.15), button);
                bounce.setToX(1.05);
                bounce.setToY(1.05);
                bounce.setAutoReverse(true);
                bounce.setCycleCount(2);
                bounce.play();
            });
        });
        pause.play();
    }

    /**
     * Kiểm tra và hiển thị popup nhập tên nếu đạt high score
     */
    private void checkAndShowHighScore() {
        if (!HighScoreManager.getInstance().isHighScore(finalScore)) {
            System.out.println("Score: " + finalScore + " - Not a high score");
            return;
        }

        System.out.println("🎉 NEW HIGH SCORE! " + finalScore);

        try {
            Scene scene = btnRestart.getScene();
            Parent gameOverRoot = scene.getRoot();

            StackPane container;
            if (gameOverRoot instanceof StackPane) {
                container = (StackPane) gameOverRoot;
            } else {
                container = new StackPane();
                container.getChildren().add(gameOverRoot);
                scene.setRoot(container);
            }

            FXMLLoader loader = new FXMLLoader();
            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/NewHighScore.fxml");

            if (resourceUrl != null) {
                loader.setLocation(resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/NewHighScore.fxml");
                loader.setLocation(fxmlFile.toURI().toURL());
            }

            Parent overlay = loader.load();

            NewHighScoreController controller = loader.getController();
            controller.setStats(finalScore, finalLevel);

            controller.setOnClose(() -> {
                container.getChildren().remove(overlay);
                System.out.println("High Score overlay closed");
            });

            container.getChildren().add(overlay);

            System.out.println(" High Score overlay displayed on top of Game Over");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error showing High Score overlay: " + ex.getMessage());
        }
    }

    /**
     * Về main menu
     */
    private void goToMainMenu() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();

            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/Main.fxml");
            FXMLLoader loader;

            if (resourceUrl != null) {
                loader = new FXMLLoader(resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/Main.fxml");
                loader = new FXMLLoader(fxmlFile.toURI().toURL());
            }

            Parent root = loader.load();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}