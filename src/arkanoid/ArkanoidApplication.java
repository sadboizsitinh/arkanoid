package arkanoid;

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
            //  Load giao diện chính (menu game)
            Parent root = FXMLLoader.load(getClass().getResource("/arkanoid/ui/fxml/Main.fxml"));

            //  Tạo scene và hiển thị
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Arkanoid Game - JavaFX Version");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" Lỗi khi load Main.fxml: " + e.getMessage());
        }
    }

}
