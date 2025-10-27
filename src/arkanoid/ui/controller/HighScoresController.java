package arkanoid.ui.controller;

import arkanoid.core.HighScore;
import arkanoid.core.HighScoreManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List;

public class HighScoresController {
    @FXML private ListView<String> listScores;
    @FXML private Button btnBack, btnClear;

    @FXML
    private void initialize() {
        loadHighScores();

        javafx.application.Platform.runLater(() -> {
            if (btnBack != null && btnBack.getScene() != null) {
                Parent root = btnBack.getScene().getRoot();
                if (root instanceof Pane) {
                    BackgroundHelper.setBackgroundImage((Pane) root, "bg-retrospace.png");
                    System.out.println("High Scores background set");
                }
            }
        });

        if (btnBack != null) {
            btnBack.setOnAction(e -> {
                try {
                    Stage stage = (Stage) btnBack.getScene().getWindow();

                    // Thử load từ resources
                    java.net.URL resourceUrl = getClass().getResource("/ui/fxml/Main.fxml");
                    FXMLLoader loader;

                    if (resourceUrl != null) {
                        loader = new FXMLLoader(resourceUrl);
                    } else {
                        // Fallback
                        java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/Main.fxml");
                        loader = new FXMLLoader(fxmlFile.toURI().toURL());
                    }

                    Parent root = loader.load();
                    stage.setScene(new Scene(root, 800, 600));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("Không thể load Main.fxml");
                }
            });
        }

        if (btnClear != null) {
            btnClear.setOnAction(e -> clearHighScores());
        }
    }

    /**
     * Load và hiển thị high scores
     */
    private void loadHighScores() {
        if (listScores == null) return;

        listScores.getItems().clear();

        List<HighScore> scores = HighScoreManager.getInstance().getHighScores();

        if (scores.isEmpty()) {
            listScores.getItems().add("No high scores yet!");
            listScores.getItems().add("");
            listScores.getItems().add("Play the game to set a record!");
        } else {
            // Header
            listScores.getItems().add("═══════════ TOP 10 HIGH SCORES ═══════════");
            listScores.getItems().add("");

            // Scores với medal
            for (int i = 0; i < scores.size(); i++) {
                HighScore score = scores.get(i);
                String medal = "";
                if (i == 0) medal = "🥇 ";
                else if (i == 1) medal = "🥈 ";
                else if (i == 2) medal = "🥉 ";
                else medal = (i + 1) + ". ";

                // Format: Medal + Name + Score + Level
                String line = String.format("%s%-15s %6d pts (Lvl %d)",
                        medal,
                        score.getPlayerName(),
                        score.getScore(),
                        score.getLevel());

                listScores.getItems().add(line);
            }

            // Footer
            listScores.getItems().add("");
            listScores.getItems().add("═══════════════════════════════════");
        }

        System.out.println("Loaded " + scores.size() + " high scores");
    }

    /**
     * Xóa tất cả high scores với confirmation
     */
    private void clearHighScores() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear High Scores");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will delete all high score records permanently!");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                HighScoreManager.getInstance().clearHighScores();
                loadHighScores();

                // Show success message
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText(null);
                success.setContentText("All high scores have been cleared!");
                success.showAndWait();

                System.out.println("All high scores cleared");
            }
        });
    }

    /**
     * Public method để refresh danh sách từ bên ngoài
     */
    public void refreshScores() {
        loadHighScores();
    }
}