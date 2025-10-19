package arkanoid.ui.controller;

import arkanoid.core.GameManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GameOverController {
    @FXML private Label lblScore, lblLevel;
    @FXML private Button btnRestart, btnBack;

    public void setStats(int score, int level) {
        if (lblScore != null) lblScore.setText("Final Score: " + score);
        if (lblLevel != null) lblLevel.setText("Level Reached: " + level);
    }

    @FXML
    private void initialize() {
        GameManager.getInstance().clearSavedGame();

        if (btnRestart != null)
            btnRestart.setOnAction(e -> {
                GameManager.getInstance().startGame();
                try {
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnRestart.getScene().getWindow();
                    javafx.fxml.FXMLLoader loader =
                            new javafx.fxml.FXMLLoader(getClass().getResource("/arkanoid/ui/fxml/GameView.fxml"));
                    javafx.scene.Parent root = loader.load();
                    stage.setScene(new javafx.scene.Scene(root, 800, 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        if (btnBack != null)
            btnBack.setOnAction(e -> {
                try {
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnBack.getScene().getWindow();
                    javafx.fxml.FXMLLoader loader =
                            new javafx.fxml.FXMLLoader(getClass().getResource("/arkanoid/ui/fxml/Main.fxml"));
                    javafx.scene.Parent root = loader.load();
                    stage.setScene(new javafx.scene.Scene(root, 800, 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
    }
}