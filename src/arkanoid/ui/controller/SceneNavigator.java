package arkanoid.ui.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {
    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void go(String resourcePath) {
        try {
            Parent root = FXMLLoader.load(SceneNavigator.class.getResource(resourcePath));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
