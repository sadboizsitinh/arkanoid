package arkanoid.ui.controller;

import arkanoid.core.HighScoreManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewHighScoreController {
    @FXML private TextField txtName;
    @FXML private Button btnSave, btnSkip;

    private int score;
    private int level;
    private Runnable onCloseCallback;

    public void setStats(int score, int level) {
        this.score = score;
        this.level = level;
        System.out.println("📊 NewHighScore popup - Score: " + score + ", Level: " + level);
    }

    /**
     * Set callback khi đóng popup
     */
    public void setOnClose(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    private void initialize() {
        // Set default text
        if (txtName != null) {
            txtName.setPromptText("Enter your name (max 15 characters)");

            // Auto focus sau khi hiển thị
            javafx.application.Platform.runLater(() -> txtName.requestFocus());

            // Enter key = Save
            txtName.setOnAction(e -> handleSave());
        }

        if (btnSave != null) {
            btnSave.setOnAction(e -> handleSave());
        }

        if (btnSkip != null) {
            btnSkip.setOnAction(e -> handleSkip());
        }
    }

    /**
     * Lưu high score và đóng popup
     */
    private void handleSave() {
        String name = txtName.getText().trim();

        if (name.isEmpty()) {
            name = "Anonymous";
        }

        // Giới hạn 15 ký tự
        if (name.length() > 15) {
            name = name.substring(0, 15);
        }

        // Thêm vào high scores
        int rank = HighScoreManager.getInstance().addHighScore(name, score, level);

        System.out.println("✅ High score saved: " + name + " - " + score + " (Rank #" + rank + ")");

        // Đóng popup trước
        closePopup();

        // Delay nhỏ rồi chuyển sang High Scores screen
        javafx.animation.PauseTransition delay =
                new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
        delay.setOnFinished(e -> goToHighScores());
        delay.play();
    }

    /**
     * Bỏ qua không lưu, đóng popup và về menu
     */
    private void handleSkip() {
        System.out.println("⏭️ Skipped saving high score");
        closePopup();

        // Delay nhỏ rồi về menu
        javafx.animation.PauseTransition delay =
                new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
        delay.setOnFinished(e -> goToMainMenu());
        delay.play();
    }

    /**
     * Đóng popup bằng callback
     */
    private void closePopup() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    /**
     * Chuyển sang màn hình high scores
     */
    private void goToHighScores() {
        try {
            Stage stage = (Stage) btnSave.getScene().getWindow();

            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/HighScores.fxml");
            FXMLLoader loader;

            if (resourceUrl != null) {
                loader = new FXMLLoader(resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/HighScores.fxml");
                loader = new FXMLLoader(fxmlFile.toURI().toURL());
            }

            Parent root = loader.load();

            // Refresh danh sách
            HighScoresController controller = loader.getController();
            if (controller != null) {
                controller.refreshScores();
            }

            stage.setScene(new Scene(root, 800, 600));
            System.out.println("✅ Navigated to High Scores screen");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("❌ Không thể load HighScores.fxml");
        }
    }

    /**
     * Về main menu
     */
    private void goToMainMenu() {
        try {
            Stage stage = (Stage) btnSkip.getScene().getWindow();

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
            System.out.println("✅ Navigated to Main Menu");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("❌ Không thể load Main.fxml");
        }
    }
}