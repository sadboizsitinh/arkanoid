package arkanoid.controller;

import arkanoid.GameManager;
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
    // MainController.java
    @FXML
    private void openSelectSkin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/fxml/SelectSkin.fxml")); // dùng path tuyệt đối
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/ui/css/style.css").toExternalForm());

        // Mở ngay trên cửa sổ chính (giống các màn khác)
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);

            System.out.println("Scene switched successfully");

        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Không thể load file FXML: " + fxmlPath);
        }
    }
}