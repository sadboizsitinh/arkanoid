package arkanoid.ui.controller;

import arkanoid.core.VersusGameManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class VersusGamePauseOverlayController {

    @FXML private StackPane root;
    @FXML private Button btnContinue;
    @FXML private Button btnNewGame;
    @FXML private Button btnMenu;

    // NEW: Reference đến VersusController VÀ Stage
    private VersusController versusController;
    private Stage stage;

    @FXML
    private void initialize() {
        System.out.println("VersusGamePauseOverlayController initialized");

        if (btnContinue != null) {
            btnContinue.setOnAction(e -> {
                System.out.println("Continue button clicked");
                handleContinue();
            });
        } else {
            System.err.println("btnContinue is NULL!");
        }

        if (btnNewGame != null) {
            btnNewGame.setOnAction(e -> {
                System.out.println("New Game button clicked");
                handleNewGame();
            });
        } else {
            System.err.println("btnNewGame is NULL!");
        }

        if (btnMenu != null) {
            btnMenu.setOnAction(e -> {
                System.out.println("Menu button clicked");
                handleMenu();
            });
        } else {
            System.err.println("btnMenu is NULL!");
        }
    }

    // NEW: Setter để nhận reference từ VersusController
    public void setVersusController(VersusController controller) {
        this.versusController = controller;
        System.out.println("VersusController reference set");
    }

    // NEW: Setter để nhận Stage reference
    public void setStage(Stage stage) {
        this.stage = stage;
        System.out.println("Stage reference set: " + stage);
    }

    private void handleContinue() {
        System.out.println("▶Continue game");

        VersusGameManager gm = VersusGameManager.getInstance();

        // FIX: Resume game trước
        if (gm.getGameState() == VersusGameManager.VersusState.PAUSED) {
            gm.togglePause();
            System.out.println("Game unpaused - state: " + gm.getGameState());
        }

        // FIX: Close overlay qua VersusController
        if (versusController != null) {
            versusController.hidePauseOverlayPublic();
        } else {
            // Fallback: tự đóng
            closeOverlay();
        }
    }

    private void handleNewGame() {
        System.out.println("New game - START");

        try {
            // FIX: Kiểm tra stage reference
            if (stage == null) {
                System.err.println("Stage is NULL! Trying to get from button...");
                stage = (Stage) btnNewGame.getScene().getWindow();

                if (stage == null) {
                    System.err.println("Cannot get stage from button either!");
                    return;
                }
            }

            System.out.println("Stage found: " + stage);

            // Stop current game
            VersusController.stopGameLoopIfAny();
            VersusGameManager.resetInstance();

            FXMLLoader loader = new FXMLLoader();
            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/VersusView.fxml");

            if (resourceUrl != null) {
                loader.setLocation(resourceUrl);
                System.out.println("Loading from resource: " + resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/VersusView.fxml");
                loader.setLocation(fxmlFile.toURI().toURL());
                System.out.println("Loading from file: " + fxmlFile.getAbsolutePath());
            }

            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            Scene scene = new Scene(root, 1296, 740);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.setResizable(false);

            System.out.println("New versus game started - Scene set");

        } catch (Exception e) {
            System.err.println("Error starting new game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMenu() {
        System.out.println("Return to main menu - START");

        try {
            // FIX: Kiểm tra stage reference
            if (stage == null) {
                System.err.println("Stage is NULL! Trying to get from button...");
                stage = (Stage) btnMenu.getScene().getWindow();

                if (stage == null) {
                    System.err.println("Cannot get stage from button either!");
                    return;
                }
            }

            System.out.println("Stage found: " + stage);

            // Stop game loop
            VersusController.stopGameLoopIfAny();
            VersusGameManager.resetInstance();

            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/Main.fxml");
            FXMLLoader loader;

            if (resourceUrl != null) {
                loader = new FXMLLoader(resourceUrl);
                System.out.println("Loading Main.fxml from resource: " + resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/Main.fxml");
                loader = new FXMLLoader(fxmlFile.toURI().toURL());
                System.out.println("Loading Main.fxml from file: " + fxmlFile.getAbsolutePath());
            }

            Parent root = loader.load();
            System.out.println("Main.fxml loaded successfully");

            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.setResizable(true);

            System.out.println("Returned to main menu - Scene set");

        } catch (Exception e) {
            System.err.println("Error returning to menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeOverlay() {
        // Remove overlay from parent
        if (root != null && root.getParent() != null) {
            try {
                ((StackPane) root.getParent()).getChildren().remove(root);
                System.out.println("Overlay closed via fallback");
            } catch (Exception e) {
                System.err.println("Error closing overlay: " + e.getMessage());
            }
        }
    }
}