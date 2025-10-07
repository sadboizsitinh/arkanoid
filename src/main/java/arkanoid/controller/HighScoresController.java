package arkanoid.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class HighScoresController {
    @FXML private ListView<String> listScores;
    @FXML private Button btnBack, btnClear;

    @FXML
    private void initialize() {
        // ✅ Thêm dữ liệu mẫu vào danh sách điểm cao
        if (listScores != null) {
            listScores.getItems().addAll(
                    "1. Player 1 - 5000",
                    "2. Player 2 - 4500",
                    "3. Player 3 - 4000",
                    "4. Player 4 - 3500",
                    "5. Player 5 - 3000"
            );
        }

        // ✅ Xử lý nút Back
        if (btnBack != null) {
            btnBack.setOnAction(e -> {
                try {
                    Stage stage = (Stage) btnBack.getScene().getWindow();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/fxml/Main.fxml"));
                    Parent root = loader.load();
                    stage.setScene(new Scene(root, 800, 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("Không thể load Main.fxml");
                }
            });
        }

        // ✅ Xử lý nút Clear
        if (btnClear != null) {
            btnClear.setOnAction(e -> {
                if (listScores != null) {
                    listScores.getItems().clear();
                    listScores.getItems().add("No high scores yet!");
                }
            });
        }
    }
}