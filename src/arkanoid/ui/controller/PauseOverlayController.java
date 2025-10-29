package arkanoid.ui.controller;

import arkanoid.core.GameManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

// Thêm các import này
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class PauseOverlayController {
    @FXML private StackPane root;
    @FXML private Button btnContinue, btnNewGame, btnMenu;

    @FXML
    private void initialize() {
        // Thêm các import này ở đầu file nếu chưa có:
        // import javafx.scene.control.TextInputDialog;
        // import java.util.Optional;
        // import arkanoid.ui.controller.SceneNavigator;

        if (btnContinue != null)
            btnContinue.setOnAction(e -> {
                GameManager gm = GameManager.getInstance();
                if (gm.getGameState() == GameManager.GameState.PAUSED) {
                    gm.setGameState(GameManager.GameState.PLAYING);
                    gm.startContinueCountdown(3);
                }
            });

        if (btnNewGame != null)
            btnNewGame.setOnAction(e -> {
                GameManager.getInstance().startGame();
                SceneNavigator.goToGame();
            });

        if (btnMenu != null)
            btnMenu.setOnAction(e -> {

                // === LOGIC MỚI: KIỂM TRA PROFILE ĐÃ TẢI ===

                GameManager gm = GameManager.getInstance();
                String currentProfile = gm.getCurrentlyLoadedProfile();

                // 1. TRƯỜNG HỢP 1: Đã tải profile (VD: "Player1")
                if (currentProfile != null) {

                    // Tự động CẬP NHẬT profile đó, không hỏi tên
                    System.out.println("Updating existing profile: " + currentProfile);
                    gm.saveGameForPlayer(currentProfile);

                    // Về menu
                    SceneNavigator.goToMenu();

                }
                // 2. TRƯỜNG HỢP 2: Đây là game mới (chưa có profile)
                else {

                    // Hiển thị dialog HỎI TÊN (như cũ)
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Save Profile");
                    dialog.setHeaderText("Bạn có muốn lưu game mới này?");
                    dialog.setContentText("Nhập tên profile (Bỏ trống để thoát không lưu):");

                    Optional<String> result = dialog.showAndWait();

                    if (result.isPresent()) {
                        String name = result.get();
                        if (name != null && !name.trim().isEmpty()) {
                            gm.saveGameForPlayer(name.trim());
                            System.out.println("New game saved to profile: " + name);
                        } else {
                            System.out.println("Exiting new game without saving.");
                        }

                        // Về menu
                        SceneNavigator.goToMenu();
                    }
                    // Nếu bấm "X" (result is empty), không làm gì cả.
                }
            });
    }
}