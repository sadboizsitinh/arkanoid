package arkanoid.controller;

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
    private Button btnContinue; // nếu có nút Continue trong Main.fxml

    @FXML
    private void initialize() {
        // Khi nhấn Start → chuyển sang GameView.fxml
        if (btnStart != null) {
            btnStart.setOnAction(e -> switchScene("/ui/fxml/GameView.fxml"));
        }

        // Khi nhấn High Scores → chuyển sang HighScores.fxml
        if (btnHighScores != null) {
            btnHighScores.setOnAction(e -> switchScene("/ui/fxml/HighScores.fxml"));
        }

        // Khi nhấn Exit → thoát chương trình
        if (btnExit != null) {
            btnExit.setOnAction(e -> System.exit(0));
        }

        // Khi nhấn Continue (nếu có) → quay lại màn chơi trước đó
        if (btnContinue != null) {
            btnContinue.setOnAction(e -> switchScene("/ui/fxml/GameView.fxml"));
        }
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
