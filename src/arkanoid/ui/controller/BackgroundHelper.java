package arkanoid.ui.controller;

import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.io.File;

/**
 * Helper class để set background image cho các scene
 */
public class BackgroundHelper {

    /**
     * Set background image từ file path
     */
    public static void setBackgroundImage(Pane pane, String imagePath) {
        try {
            Image image;

            // Thử load từ resources trước
            String resourcePath = "/assets/images/" + imagePath;
            if (BackgroundHelper.class.getResource(resourcePath) != null) {
                image = new Image(BackgroundHelper.class.getResourceAsStream(resourcePath));
            } else {
                // Fallback: load từ file system
                File imageFile = new File("src/arkanoid/assets/images/" + imagePath);
                if (imageFile.exists()) {
                    image = new Image(imageFile.toURI().toString());
                } else {
                    System.err.println("Image not found: " + imagePath);
                    return;
                }
            }

            BackgroundImage backgroundImage = new BackgroundImage(
                    image,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(
                            BackgroundSize.AUTO,
                            BackgroundSize.AUTO,
                            false,
                            false,
                            false,
                            true  // Cover
                    )
            );

            pane.setBackground(new Background(backgroundImage));
            System.out.println("Background image set: " + imagePath);

        } catch (Exception e) {
            System.err.println("Error setting background: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set background với overlay tối
     */
    public static void setBackgroundWithOverlay(Pane pane, String imagePath, double opacity) {
        setBackgroundImage(pane, imagePath);

        // Thêm overlay tối
        Pane overlay = new Pane();
        overlay.setStyle(String.format(
                "-fx-background-color: rgba(0, 0, 0, %.2f); -fx-pref-width: 10000; -fx-pref-height: 10000;",
                opacity
        ));

        if (pane instanceof StackPane) {
            ((StackPane) pane).getChildren().add(0, overlay);
        }
    }
}