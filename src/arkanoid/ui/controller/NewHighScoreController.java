package arkanoid.ui.controller;

import arkanoid.core.HighScoreManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import arkanoid.core.HighScoreManager;

public class NewHighScoreController {
    @FXML private TextField txtName;
    @FXML private Button btnSave, btnSkip;

    private int score;
    private int level;
    private Runnable onCloseCallback;

    public void setStats(int score, int level) {
        this.score = score;
        this.level = level;
        System.out.println("NewHighScore popup - Score: " + score + ", Level: " + level);
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
        if (name.isEmpty()) name = "Anonymous";
        if (name.length() > 15) name = name.substring(0, 15);

        int rank = HighScoreManager.getInstance().addHighScore(name, score, level);
        System.out.println(" High score saved: " + name + " - " + score + " (Rank #" + rank + ")");
        System.out.println(" Staying on Game Over screen");

        closePopup(); // CHỈ ĐÓNG POPUP, KHÔNG CHUYỂN SCENE
    }

    /**
     * Bỏ qua không lưu, đóng popup và về menu
     */
    private void handleSkip() {
        System.out.println(" Skipped saving high score");
        System.out.println(" Staying on Game Over screen");
        closePopup();
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
            // 🔹 Tìm stage đang hiển thị
            javafx.stage.Stage stage = null;
            for (javafx.stage.Window window : javafx.stage.Stage.getWindows()) {
                if (window instanceof javafx.stage.Stage && window.isShowing()) {
                    stage = (javafx.stage.Stage) window;
                    break;
                }
            }
            if (stage == null) {
                System.err.println("No active stage found to show HighScores!");
                return;
            }

            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/HighScores.fxml");
            javafx.fxml.FXMLLoader loader;

            if (resourceUrl != null) {
                loader = new javafx.fxml.FXMLLoader(resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/HighScores.fxml");
                loader = new javafx.fxml.FXMLLoader(fxmlFile.toURI().toURL());
            }

            javafx.scene.Parent root = loader.load();

            // Refresh danh sách
            HighScoresController controller = loader.getController();
            if (controller != null) {
                controller.refreshScores();
            }

            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.show();
            System.out.println("Navigated to High Scores screen");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Không thể load HighScores.fxml");
        }
    }

    /**
     * Về main menu
     */
    private void goToMainMenu() {
        try {
            // 🔹 Lấy stage đang mở (an toàn)
            javafx.stage.Stage stage = null;
            for (javafx.stage.Window window : javafx.stage.Stage.getWindows()) {
                if (window instanceof javafx.stage.Stage && window.isShowing()) {
                    stage = (javafx.stage.Stage) window;
                    break;
                }
            }
            if (stage == null) {
                System.err.println("No active stage found to show Main Menu!");
                return;
            }

            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/Main.fxml");
            javafx.fxml.FXMLLoader loader;

            if (resourceUrl != null) {
                loader = new javafx.fxml.FXMLLoader(resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/Main.fxml");
                loader = new javafx.fxml.FXMLLoader(fxmlFile.toURI().toURL());
            }

            javafx.scene.Parent root = loader.load();
            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.show();
            System.out.println("Navigated to Main Menu");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Không thể load Main.fxml");
        }
    }
}