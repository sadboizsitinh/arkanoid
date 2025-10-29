package arkanoid.ui.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * (PHIÊN BẢN SỬA LỖI HOÀN CHỈNH)
 * Lớp điều hướng scene duy nhất cho toàn bộ ứng dụng.
 * Hãy THAY THẾ TOÀN BỘ file SceneNavigator.java cũ bằng file này.
 */
public class SceneNavigator {
    private static Stage stage;

    /**
     * Hàm này được gọi bởi ArkanoidApplication.java để giao cửa sổ chính
     */
    public static void setStage(Stage s) {
        stage = s;
    }

    /**
     * Phương thức 'go' chính, dùng để chuyển scene
     * @param fxmlPath Đường dẫn FXML (ví dụ: "/ui/fxml/Main.fxml")
     * @param width Chiều rộng
     * @param height Chiều cao
     */
    public static void go(String fxmlPath, int width, int height) {
        try {
            if (stage == null) {
                System.err.println("SceneNavigator: LỖI: Stage chưa được set! Hãy gọi setStage() trong ArkanoidApplication.");
                return;
            }

            // 1. Xây dựng đường dẫn resource chuẩn
            String resourcePath = "/arkanoid" + fxmlPath;
            java.net.URL resourceUrl = SceneNavigator.class.getResource(resourcePath);

            // 2. Thử fallback nếu không tìm thấy (cho một số cấu hình IDE)
            if (resourceUrl == null) {
                System.err.println("SceneNavigator: Không tìm thấy resource tại " + resourcePath + ". Đang thử fallback...");
                // Thử đường dẫn không có /arkanoid (nếu 'src' là classpath root)
                String fallbackPath = fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
                resourceUrl = SceneNavigator.class.getResource(fallbackPath);

                if (resourceUrl != null) {
                    System.out.println("SceneNavigator: Fallback thành công tại: " + fallbackPath);
                }
            }

            // 3. Nếu vẫn null, báo lỗi và dừng
            if (resourceUrl == null) {
                System.err.println("SceneNavigator: LỖI NGHIÊM TRỌNG: Không thể tìm thấy file FXML.");
                System.err.println("   Đã thử: " + resourcePath);
                System.err.println("   Và: " + (fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath));
                System.err.println("   Hãy đảm bảo file .fxml được copy vào thư mục build/out!");
                return;
            }

            // Dừng các game loop (nếu có)
            GameController.stopGameLoopIfAny();
            // VersusController.stopGameLoopIfAny(); // (Bạn có thể thêm dòng này nếu có VersusController)

            // 4. Tải và hiển thị scene
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.setResizable(false);

            System.out.println("SceneNavigator: Đã chuyển tới " + resourcePath);

        } catch (Exception e) {
            System.err.println("SceneNavigator: Lỗi khi chuyển scene tới " + fxmlPath);
            e.printStackTrace();
        }
    }

    // --- Các hàm tiện ích ---

    public static void goToMenu() {
        go("/ui/fxml/Main.fxml", 800, 600);
    }

    public static void goToGame() {
        go("/ui/fxml/GameView.fxml", 1000, 600);
    }

    public static void goToHighScores() {
        go("/ui/fxml/HighScores.fxml", 800, 600);
    }

    public static void goToVersus() {
        go("/ui/fxml/VersusView.fxml", 1296, 740);
    }

    // (Bạn có thể thêm các hàm khác như goToGameOver tại đây nếu cần)
}