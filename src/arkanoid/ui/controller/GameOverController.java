package arkanoid.ui.controller;

// === THÊM CÁC IMPORT NÀY ===
import arkanoid.core.GameManager;
// import arkanoid.core.GameStatePersistence; // (Bạn có thể không cần cái này nữa)
import arkanoid.core.HighScoreManager;
// SceneNavigator là file điều hướng chính của chúng ta
import arkanoid.ui.controller.SceneNavigator;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
// import javafx.scene.Scene; // (Không cần import Scene nữa)
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
// import javafx.stage.Stage; // (Không cần import Stage nữa)
import javafx.util.Duration;

public class GameOverController {
    @FXML private Label lblScore, lblLevel;
    @FXML private Button btnRestart, btnBack;

    private int finalScore;
    private int finalLevel;

    /**
     * === HÀM NÀY GIỮ NGUYÊN ===
     * Dùng để nhận điểm số và level từ GameController
     */
    public void setStats(int score, int level) {
        this.finalScore = score;
        this.finalLevel = level;

        System.out.println("============================================\n" +
                "   GameOver.setStats() called\n" +
                "   Score: " + score + ", Level: " + level + "\n" +
                "============================================");

        if (lblScore != null) {
            // (Toàn bộ code animation cho điểm số của bạn nằm ở đây và được giữ nguyên)
            lblScore.setOpacity(0);
            lblScore.setScaleX(0.5);
            lblScore.setScaleY(0.5);

            PauseTransition delay = new PauseTransition(Duration.millis(300));
            delay.setOnFinished(e -> {
                lblScore.setText(String.format("%,d", finalScore));
                lblLevel.setText(String.format("Level %d", finalLevel));

                FadeTransition ft = new FadeTransition(Duration.millis(500), lblScore);
                ft.setToValue(1.0);
                ScaleTransition st = new ScaleTransition(Duration.millis(500), lblScore);
                st.setToX(1.0);
                st.setToY(1.0);

                ParallelTransition pt = new ParallelTransition(ft, st);
                pt.play();
            });
            delay.play();
        } else {
            System.err.println("GameOverController: lblScore is null in setStats!");
        }
    }

    /**
     * === THAY ĐỔI QUAN TRỌNG NẰM Ở ĐÂY ===
     * Hàm này được FXML tự động gọi sau khi load.
     * Chúng ta gán hành động cho các nút (Button) ở đây.
     */
    @FXML
    private void initialize() {

        // Gán hành động cho nút "Play Again" (btnRestart)
        if (btnRestart != null) {
            btnRestart.setOnAction(e -> {
                // 1. Yêu cầu GameManager bắt đầu một game mới
                GameManager.getInstance().startGame();

                // 2. Yêu cầu SceneNavigator chuyển cảnh về GameView
                SceneNavigator.goToGame();
            });
        }

        // Gán hành động cho nút "Main Menu" (btnBack)
        if (btnBack != null) {
            btnBack.setOnAction(e -> {
                // 1. Yêu cầu SceneNavigator chuyển cảnh về Main Menu
                SceneNavigator.goToMenu();
            });
        }
    }

    /**
     * === HÀM NÀY ĐÃ BỊ XÓA BỎ ===
     * Logic của hàm này đã được chuyển lên `initialize()`
     * và sử dụng `SceneNavigator.goToMenu()`.
     * Xóa bỏ hàm này sẽ giúp code sạch sẽ hơn.
     */
    // private void goToMainMenu() {
    //    // (Toàn bộ code cũ của bạn về FXMLLoader, Stage, Scene... ở đây sẽ bị xóa)
    // }


    /**
     * === HÀM NÀY GIỮ NGUYÊN ===
     * (Nếu bạn có hàm này để hiện popup NewHighScore, hãy giữ nguyên nó)
     */
    private void showHighScoreOverlay(StackPane container) {
        try {
            // (Code này vẫn dùng FXMLLoader vì nó là TẢI OVERLAY, không phải CHUYỂN SCENE)
            // (Code này của bạn có vẻ đúng nên tôi giữ nguyên)
            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/NewHighScore.fxml");
            FXMLLoader loader;

            if (resourceUrl != null) {
                loader = new FXMLLoader(resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/NewHighScore.fxml");
                loader = new FXMLLoader(fxmlFile.toURI().toURL());
            }

            Parent overlay = loader.load();

            NewHighScoreController controller = loader.getController();
            controller.setStats(finalScore, finalLevel);

            controller.setOnClose(() -> {
                container.getChildren().remove(overlay);
                System.out.println("High Score overlay closed");
            });

            container.getChildren().add(overlay);

            System.out.println("✅ High Score overlay displayed on top of Game Over");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error showing High Score overlay: " + ex.getMessage());
        }
    }
}