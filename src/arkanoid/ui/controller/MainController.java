package arkanoid.ui.controller;

import arkanoid.core.GameManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    @FXML
    private Button btnStart;

    @FXML
    private Button btnHighScores;

    @FXML
    private Button btnExit;

    @FXML
    private Button btnContinue;

    @FXML
    private void initialize() {
        //  Kiểm tra và hiển thị/ẩn nút Continue
        if (btnContinue != null) {
            boolean hasSavedGame = GameManager.getInstance().hasSavedGame();
            btnContinue.setVisible(hasSavedGame);
            btnContinue.setManaged(hasSavedGame);

            if (hasSavedGame) {
                System.out.println(" Found saved game - Continue button enabled");
            } else {
                System.out.println(" No saved game - Continue button hidden");
            }
        }

        // Khi nhấn Start → start game mới
        if (btnStart != null) {
            btnStart.setOnAction(e -> {
                // Clear saved game và start mới
                GameManager.getInstance().clearSavedGame();
                GameManager.getInstance().startGame();
                switchScene("/ui/fxml/GameView.fxml");
            });
        }

        // Khi nhấn High Scores → chuyển sang HighScores.fxml
        if (btnHighScores != null) {
            btnHighScores.setOnAction(e -> switchScene("/ui/fxml/HighScores.fxml"));
        }

        // Khi nhấn Exit → thoát chương trình
        if (btnExit != null) {
            btnExit.setOnAction(e -> System.exit(0));
        }

        if (btnContinue != null) {
            btnContinue.setOnAction(e -> {
                if (GameManager.getInstance().hasSavedGame()) {
                    GameManager.getInstance().continueGame();
                    System.out.println(" Continue game called, state: " + GameManager.getInstance().getGameState());
                    switchScene("/ui/fxml/GameView.fxml");
                } else {
                    System.err.println(" No saved game to continue!");
                }
            });
        }
    }

    @FXML
    private void openSelectSkin(ActionEvent event) throws IOException {
        // Load từ file thay vì resources
        java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/SelectSkin.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 600);

        // Load CSS từ file
        java.io.File cssFile = new java.io.File("src/arkanoid/ui/css/style.css");
        if (cssFile.exists()) {
            scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
        }

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }


    /**
     * Chuyển sang scene khác theo đường dẫn FXML.
     */
    private void switchScene(String fxmlPath) {
        try {
            System.out.println("Switching to: " + fxmlPath);

            Stage stage = (Stage) btnStart.getScene().getWindow();

            // Chuyển đổi đường dẫn: /ui/fxml/GameView.fxml → src/arkanoid/ui/fxml/GameView.fxml
            String filePath = "src/arkanoid" + fxmlPath;
            java.io.File fxmlFile = new java.io.File(filePath);

            if (!fxmlFile.exists()) {
                System.err.println("❌ FXML file not found: " + fxmlFile.getAbsolutePath());
                return;
            }

            System.out.println("✅ Loading FXML from: " + fxmlFile.getAbsolutePath());

            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            // ✅ CẬP NHẬT: Nếu là GameView thì dùng width 1000 (800 canvas + 200 panel)
            int width = fxmlPath.contains("GameView") ? 1000 : 800;
            Scene scene = new Scene(root, width, 600);
            stage.setScene(scene);

            System.out.println("✅ Scene switched successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("❌ Không thể load file FXML: " + fxmlPath);
        }
    }
}