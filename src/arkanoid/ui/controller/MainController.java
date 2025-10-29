package arkanoid.ui.controller;

// Thêm lại các import này
import arkanoid.core.GameManager;
import arkanoid.core.GameStateSnapshot;
import arkanoid.core.PlayerSave;
import arkanoid.core.SaveGameManager;
import arkanoid.utils.SoundManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List; // Thêm lại
import javafx.scene.control.Alert; // Thêm lại
import javafx.scene.control.ChoiceDialog; // Thêm lại

/**
 * (PHIÊN BẢN SỬA LỖI - QUAY LẠI LOGIC LƯU PROFILE)
 * Sử dụng SaveGameManager để lưu 10 slot theo tên.
 */
public class MainController {

    @FXML private Button btnStart;
    @FXML private Button btnHighScores;
    @FXML private Button btnExit;
    @FXML private Button btnContinue;
    @FXML private Button btnVersus;

    @FXML
    private void initialize() {
        SoundManager.playBackground("Arkanoid_sound_menu.wav", 0.3);

        javafx.application.Platform.runLater(() -> {
            if (btnStart.getScene() != null && btnStart.getScene().getRoot() instanceof Pane) {
                BackgroundHelper.setBackgroundImage(
                        (Pane) btnStart.getScene().getRoot(),
                        "bg-retrospace.png"
                );
            }
        });

        // === GÁN SỰ KIỆN CHO TẤT CẢ CÁC NÚT ===
        if (btnStart != null) {
            btnStart.setOnAction(e -> handleStartGame(e));
        }
        if (btnHighScores != null) {
            btnHighScores.setOnAction(e -> handleHighScores(e));
        }
        if (btnVersus != null) {
            btnVersus.setOnAction(e -> handleVersusGame(e));
        }
        if (btnContinue != null) {
            btnContinue.setOnAction(e -> handleContinueGame(e));
        }
        if (btnExit != null) {
            btnExit.setOnAction(e -> handleExitGame(e));
        }

        // [SỬA LỖI] Dùng SaveGameManager (quản lý 10 slot)
        btnContinue.setDisable(!SaveGameManager.getInstance().hasSavedGames());
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        // [SỬA LỖI] Bắt đầu game mới KHÔNG xóa profile của người khác
        // GameManager.getInstance().clearSavedGame(); // <-- BỎ DÒNG NÀY
        GameManager.getInstance().startGame();
        SceneNavigator.goToGame();
    }

    @FXML
    private void handleContinueGame(ActionEvent event) {
        // [SỬA LỖI] Lấy danh sách profile từ SaveGameManager
        List<PlayerSave> saves = SaveGameManager.getInstance().getSavedGames();

        if (saves == null || saves.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Không có game đã lưu");
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy profile nào.");
            alert.showAndWait();
            return;
        }

        // [SỬA LỖI] Hiển thị BẢNG CHỌN (ChoiceDialog)
        ChoiceDialog<PlayerSave> dialog = new ChoiceDialog<>(saves.get(0), saves);
        dialog.setTitle("Chọn Profile");
        dialog.setHeaderText("Chọn một lượt chơi để tiếp tục:");
        dialog.setContentText("Profile:");

        dialog.showAndWait().ifPresent(selectedSave -> {
            // Tải snapshot từ profile đã chọn
            GameManager.getInstance().restoreFromSnapshot(selectedSave.getSnapshot());
            GameManager.getInstance().setCurrentlyLoadedProfile(selectedSave.getPlayerName());
            SceneNavigator.goToGame();
        });
    }

    @FXML
    private void handleHighScores(ActionEvent event) {
        SceneNavigator.goToHighScores();
    }

    @FXML
    private void handleVersusGame(ActionEvent event) {
        SceneNavigator.goToVersus();
    }

    @FXML
    private void handleExitGame(ActionEvent event) {
        Stage stage = (Stage) btnExit.getScene().getWindow();
        stage.close();
    }
}