package arkanoid;

import arkanoid.core.GameManager;
import arkanoid.core.GameStatePersistence;
import arkanoid.core.GameStateSnapshot;
import arkanoid.ui.controller.SceneNavigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application class.
 * Khởi chạy giao diện chính từ FXML (Main.fxml).
 * Khi nhấn Start Game, GameController sẽ tự khởi động game loop.
 */
public class ArkanoidApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load giao diện chính (menu game)
            Parent root = FXMLLoader.load(getClass().getResource("/arkanoid/ui/fxml/Main.fxml"));

            // Tạo scene và hiển thị
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Arkanoid Game - JavaFX Version");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            // === 2. THÊM DÒNG NÀY VÀO ===
            // Giao Stage chính cho SceneNavigator để nó có thể chuyển cảnh
            SceneNavigator.setStage(primaryStage);
            // =============================

            // Lưu game khi đóng cửa sổ (nhấn X)
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Window closing...");
                saveGameOnExit();
            });

            // ... (phần còn lại của hàm) ...

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi load Main.fxml: " + e.getMessage());
        }
    }

    /**
     * (HÀM ĐÃ SỬA LỖI)
     * Vô hiệu hóa auto-save.
     * Việc lưu game (có hỏi tên) được xử lý trong PauseOverlayController.
     */
    private void saveGameOnExit() {
        System.out.println("Auto-save on exit is disabled. Please save via the Pause Menu.");
        // Không làm gì cả
    }
}