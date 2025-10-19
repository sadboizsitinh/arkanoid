package arkanoid.ui.controller;

import arkanoid.core.GameManager;
import arkanoid.core.HighScoreManager;
import javafx.animation.PauseTransition;
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
        System.out.println("📊 GameOver.setStats() called");
        System.out.println("   Score: " + score);
        System.out.println("   Level: " + level);
        System.out.println("   lblScore is null? " + (lblScore == null));
        System.out.println("   lblLevel is null? " + (lblLevel == null));
        System.out.println("   btnRestart is null? " + (btnRestart == null));
        System.out.println("============================================");

        if (lblScore != null) lblScore.setText("Final Score: " + score);
        if (lblLevel != null) lblLevel.setText("Level Reached: " + level);

        // ✅ Dùng PauseTransition để delay trước khi check high score
        PauseTransition delay = new PauseTransition(Duration.seconds(0.001));
        delay.setOnFinished(event -> {
            System.out.println("⏰ Delay finished, calling checkAndShowHighScore()");
            checkAndShowHighScore();
        });
        delay.play();
        System.out.println("⏱️ PauseTransition started");
    }

    @FXML
    private void initialize() {
        GameManager.getInstance().clearSavedGame();

        if (btnRestart != null) {
            btnRestart.setOnAction(e -> {
                GameManager.getInstance().startGame();
                try {
                    Stage stage = (Stage) btnRestart.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/fxml/GameView.fxml"));
                    Parent root = loader.load();
                    stage.setScene(new Scene(root, 800, 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (btnBack != null) {
            btnBack.setOnAction(e -> goToMainMenu());
        }
    }

    /**
     * Kiểm tra và hiển thị popup nhập tên nếu đạt high score
     */
    private void checkAndShowHighScore() {
        // Kiểm tra có phải high score không
        if (!HighScoreManager.getInstance().isHighScore(finalScore)) {
            System.out.println("📊 Score: " + finalScore + " - Not a high score");
            return;
        }

        System.out.println("🎉 NEW HIGH SCORE! " + finalScore);

        try {
            // Lấy Scene và Root hiện tại
            Scene scene = btnRestart.getScene();
            Parent gameOverRoot = scene.getRoot();

            // Nếu root chưa phải StackPane, wrap nó
            StackPane container;
            if (gameOverRoot instanceof StackPane) {
                container = (StackPane) gameOverRoot;
            } else {
                container = new StackPane();
                container.getChildren().add(gameOverRoot);
                scene.setRoot(container);
            }

            // Load overlay NewHighScore
            FXMLLoader loader = new FXMLLoader();
            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/NewHighScore.fxml");

            if (resourceUrl != null) {
                loader.setLocation(resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/NewHighScore.fxml");
                loader.setLocation(fxmlFile.toURI().toURL());
            }

            Parent overlay = loader.load();

            // Lấy controller của overlay
            NewHighScoreController controller = loader.getController();
            controller.setStats(finalScore, finalLevel);

            // Set callback khi đóng overlay
            controller.setOnClose(() -> {
                // Xóa overlay khỏi container
                container.getChildren().remove(overlay);
                System.out.println("✅ High Score overlay closed");
            });

            // Thêm overlay lên trên (sẽ che phủ Game Over với nền mờ đậm)
            container.getChildren().add(overlay);

            System.out.println("✅ High Score overlay displayed on top of Game Over");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("❌ Error showing High Score overlay: " + ex.getMessage());
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