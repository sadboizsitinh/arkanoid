package arkanoid.ui.controller;

import arkanoid.core.GameManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class PauseOverlayController {
    @FXML private StackPane root;
    @FXML private Button btnContinue, btnNewGame, btnMenu;

    @FXML
    private void initialize() {
        if (btnContinue != null)
            btnContinue.setOnAction(e -> {
                GameManager gm = GameManager.getInstance();

                // Nếu đang tạm dừng → quay lại chế độ chơi và bắt đầu countdown
                if (gm.getGameState() == GameManager.GameState.PAUSED) {
                    gm.setGameState(GameManager.GameState.PLAYING);
                    gm.startContinueCountdown(3); // Đếm ngược 3 giây
                }
            });


        if (btnNewGame != null)
            btnNewGame.setOnAction(e -> {
                // Clear saved game và start mới
                GameManager.getInstance().clearSavedGame();
                GameManager.getInstance().startGame();
            });

        if (btnMenu != null)
            btnMenu.setOnAction(e -> {
                try {
                    //  LƯU GAME STATE TRƯỚC KHI VỀ MENU
                    GameManager.getInstance().saveGameState();

                    javafx.stage.Stage stage = (javafx.stage.Stage) btnMenu.getScene().getWindow();
                    java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/Main.fxml");
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlFile.toURI().toURL());
                    GameController.stopGameLoopIfAny();
                    javafx.scene.Parent root = loader.load();
                    stage.setScene(new javafx.scene.Scene(root, 800, 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
    }
}