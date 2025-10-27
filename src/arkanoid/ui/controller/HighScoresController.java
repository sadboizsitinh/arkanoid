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
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.util.List;

public class HighScoresController {
    @FXML private VBox scoresContainer;
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

                    java.net.URL resourceUrl = getClass().getResource("/ui/fxml/Main.fxml");
                    FXMLLoader loader;

                    if (resourceUrl != null) {
                        loader = new FXMLLoader(resourceUrl);
                    } else {
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
     * Load và hiển thị high scores với giao diện galaxy đẹp
     */
    private void loadHighScores() {
        if (scoresContainer == null) return;

        scoresContainer.getChildren().clear();

        List<HighScore> scores = HighScoreManager.getInstance().getHighScores();

        if (scores.isEmpty()) {
            // Empty state với galaxy theme
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setStyle("-fx-padding: 60;");

            Label emptyIcon = new Label("🌟");
            emptyIcon.setStyle("-fx-font-size: 80px;");

            Label emptyText = new Label("No High Scores Yet");
            emptyText.setStyle(
                    "-fx-font-size: 28px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: linear-gradient(to right, #06b6d4, #a78bfa); " +
                            "-fx-effect: dropshadow(gaussian, rgba(6, 182, 212, 0.6), 10, 0.5, 0, 0);"
            );

            Label emptyHint = new Label("Play the game to set a record!");
            emptyHint.setStyle(
                    "-fx-font-size: 16px; " +
                            "-fx-text-fill: #8b93a5; " +
                            "-fx-font-style: italic;"
            );

            emptyState.getChildren().addAll(emptyIcon, emptyText, emptyHint);
            scoresContainer.getChildren().add(emptyState);
        } else {
            // Hiển thị từng điểm số với galaxy card style
            for (int i = 0; i < scores.size(); i++) {
                HighScore score = scores.get(i);
                HBox scoreCard = createScoreCard(score, i);
                scoresContainer.getChildren().add(scoreCard);
            }
        }

        System.out.println("Loaded " + scores.size() + " high scores with galaxy theme");
    }

    /**
     * Tạo card đẹp cho mỗi high score
     */
    private HBox createScoreCard(HighScore score, int rank) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefHeight(70);
        card.setMaxWidth(650);

        // Màu sắc và style theo rank
        String bgColor, borderColor, glowColor, medalEmoji;

        if (rank == 0) {
            // Top 1 - Vàng rực rỡ
            bgColor = "linear-gradient(to right, rgba(251, 191, 36, 0.15), rgba(252, 211, 77, 0.1))";
            borderColor = "#fbbf24";
            glowColor = "rgba(251, 191, 36, 0.5)";
            medalEmoji = "🥇";
        } else if (rank == 1) {
            // Top 2 - Bạc sáng
            bgColor = "linear-gradient(to right, rgba(203, 213, 225, 0.15), rgba(226, 232, 240, 0.1))";
            borderColor = "#cbd5e1";
            glowColor = "rgba(203, 213, 225, 0.5)";
            medalEmoji = "🥈";
        } else if (rank == 2) {
            // Top 3 - Đồng
            bgColor = "linear-gradient(to right, rgba(251, 146, 60, 0.15), rgba(253, 186, 116, 0.1))";
            borderColor = "#fb923c";
            glowColor = "rgba(251, 146, 60, 0.5)";
            medalEmoji = "🥉";
        } else {
            // Còn lại - Galaxy cyan/purple
            bgColor = "linear-gradient(to right, rgba(6, 182, 212, 0.1), rgba(167, 139, 250, 0.1))";
            borderColor = "#06b6d4";
            glowColor = "rgba(6, 182, 212, 0.4)";
            medalEmoji = String.valueOf(rank + 1) + ".";
        }

        card.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 12 20; " +
                        "-fx-effect: dropshadow(gaussian, " + glowColor + ", 12, 0.5, 0, 0);"
        );

        // Rank/Medal (80px)
        Label lblRank = new Label(medalEmoji);
        lblRank.setPrefWidth(80);
        lblRank.setAlignment(Pos.CENTER);
        lblRank.setStyle(
                "-fx-font-size: " + (rank < 3 ? "36px" : "28px") + "; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );

        // Player Name (250px)
        Label lblName = new Label(score.getPlayerName());
        lblName.setPrefWidth(250);
        lblName.setStyle(
                "-fx-font-size: 22px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.8), 4, 0.5, 0, 2);"
        );

        // Score (180px)
        VBox scoreBox = new VBox(2);
        scoreBox.setPrefWidth(180);
        scoreBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblScore = new Label(String.format("%,d", score.getScore()));
        lblScore.setStyle(
                "-fx-font-size: 26px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + borderColor + "; " +
                        "-fx-effect: dropshadow(gaussian, " + glowColor + ", 8, 0.6, 0, 0);"
        );

        Label lblPoints = new Label("points");
        lblPoints.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #8b93a5; " +
                        "-fx-font-style: italic;"
        );

        scoreBox.getChildren().addAll(lblScore, lblPoints);

        // Level (100px)
        VBox levelBox = new VBox(2);
        levelBox.setPrefWidth(100);
        levelBox.setAlignment(Pos.CENTER);

        Label lblLevel = new Label(String.valueOf(score.getLevel()));
        lblLevel.setStyle(
                "-fx-font-size: 22px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );

        Label lblLevelText = new Label("level");
        lblLevelText.setStyle(
                "-fx-font-size: 11px; " +
                        "-fx-text-fill: #8b93a5;"
        );

        levelBox.getChildren().addAll(lblLevel, lblLevelText);

        card.getChildren().addAll(lblRank, lblName, scoreBox, levelBox);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    card.getStyle() +
                            "-fx-scale-x: 1.02; " +
                            "-fx-scale-y: 1.02; " +
                            "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    card.getStyle().replace("-fx-scale-x: 1.02; ", "")
                            .replace("-fx-scale-y: 1.02; ", "")
                            .replace("-fx-cursor: hand;", "")
            );
        });

        return card;
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