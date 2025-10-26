package arkanoid.ui.controller;

import arkanoid.core.GameManager;
import arkanoid.core.GameStatePersistence;
import arkanoid.utils.SoundManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
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
        SoundManager.playBackground("Arkanoid_sound_menu.wav", 0.3);

        // Set background
        javafx.application.Platform.runLater(() -> {
            if (btnStart.getScene() != null && btnStart.getScene().getRoot() instanceof Pane) {
                BackgroundHelper.setBackgroundImage(
                        (Pane) btnStart.getScene().getRoot(),
                        "bg-retrospace.png"
                );
            }
        });

        // ✅ Kiểm tra file save khi mở app
        if (btnContinue != null) {
            boolean hasSavedGame = GameStatePersistence.hasSaveFile();
            btnContinue.setVisible(hasSavedGame);
            btnContinue.setManaged(hasSavedGame);

            if (hasSavedGame) {
                System.out.println("💾 Found saved game file - Continue button enabled");
            } else {
                System.out.println("ℹ️ No saved game file - Continue button hidden");
            }
        }

        // Khi nhấn Start → xóa file save và start mới
        if (btnStart != null) {
            btnStart.setOnAction(e -> {
                GameStatePersistence.deleteSaveFile(); // ✅ Xóa file save cũ
                GameManager.getInstance().startGame();
                switchScene("/ui/fxml/GameView.fxml");
            });
        }

        // Khi nhấn High Scores
        if (btnHighScores != null) {
            btnHighScores.setOnAction(e -> switchScene("/ui/fxml/HighScores.fxml"));
        }

        // Khi nhấn Exit
        if (btnExit != null) {
            btnExit.setOnAction(e -> System.exit(0));
        }

        // ✅ Khi nhấn Continue → load từ file
        if (btnContinue != null) {
            btnContinue.setOnAction(e -> {
                if (GameStatePersistence.hasSaveFile()) {
                    // ✅ Load game state
                    GameManager.getInstance().continueGame();

                    // ✅ BẮT ĐẦU COUNTDOWN 3 GIÂY
                    GameManager.getInstance().startCountdownFromMenu(3);

                    System.out.println("💾 Continue game from file with countdown");
                    switchScene("/ui/fxml/GameView.fxml");
                } else {
                    System.err.println("❌ No saved game file to continue!");
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