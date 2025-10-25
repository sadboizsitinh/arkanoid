package arkanoid;

import arkanoid.core.GameManager;
import arkanoid.core.GameStatePersistence;
import arkanoid.core.GameStateSnapshot;
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

            // ✅ Lưu game khi đóng cửa sổ (nhấn X)
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("🚪 Window closing...");
                saveGameOnExit();
            });

            // ✅ Lưu game khi JVM shutdown (Alt+F4, kill process, etc.)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("🛑 JVM shutting down...");
                saveGameOnExit();
            }));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi khi load Main.fxml: " + e.getMessage());
        }
    }

    /**
     * ✅ Lưu game nếu đang chơi hoặc pause
     */
    private void saveGameOnExit() {
        try {
            GameManager gm = GameManager.getInstance();

            // Chỉ lưu nếu đang PLAYING hoặc PAUSED
            if (gm.getGameState() == GameManager.GameState.PLAYING ||
                    gm.getGameState() == GameManager.GameState.PAUSED) {

                System.out.println("💾 Auto-saving game before exit...");
                GameStateSnapshot snapshot = GameStateSnapshot.createSnapshot(gm);
                boolean success = GameStatePersistence.saveToFile(snapshot);

                if (success) {
                    System.out.println("✅ Game auto-saved successfully!");
                } else {
                    System.err.println("❌ Failed to auto-save game!");
                }
            } else {
                System.out.println("ℹ️ No active game to save (state: " + gm.getGameState() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Error during auto-save: " + e.getMessage());
            e.printStackTrace();
        }
    }
}