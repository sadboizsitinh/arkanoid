package arkanoid.ui.controller;

import arkanoid.core.VersusGameManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VersusGameOverController {

    @FXML
    private Label lblWinner;

    @FXML
    private Label lblScore1;

    @FXML
    private Label lblScore2;

    @FXML
    private Label lblResultP1;

    @FXML
    private Label lblResultP2;

    @FXML
    private Button btnPlayAgain;

    @FXML
    private Button btnMainMenu;

    private int score1;
    private int score2;
    private String winner;

    public void setStats(int score1, int score2, String winner) {
        this.score1 = score1;
        this.score2 = score2;
        this.winner = winner;

        System.out.println("=== VERSUS GAME OVER ===");
        System.out.println("Player 1: " + score1);
        System.out.println("Player 2: " + score2);
        System.out.println("Winner: " + winner);

        displayResults();
    }

    @FXML
    private void initialize() {
        if (btnPlayAgain != null) {
            btnPlayAgain.setOnAction(e -> playAgain());
        }

        if (btnMainMenu != null) {
            btnMainMenu.setOnAction(e -> goToMainMenu());
        }
    }

    private void displayResults() {
        // Hiển thị kết quả với animation
        javafx.application.Platform.runLater(() -> {
            if (lblWinner != null) {
                String winnerText;
                String winnerColor;

                switch (winner) {
                    case "P1":
                        winnerText = "🏆 PLAYER 1 WINS! 🏆";
                        winnerColor = "#7CFC00"; // Xanh lá
                        break;
                    case "P2":
                        winnerText = "🏆 PLAYER 2 WINS! 🏆";
                        winnerColor = "#00BFFF"; // Xanh dương
                        break;
                    default:
                        winnerText = "🤝 DRAW! 🤝";
                        winnerColor = "#fbbf24"; // Vàng
                        break;
                }

                lblWinner.setText(winnerText);
                lblWinner.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: " + winnerColor + ";");

                // Animation cho winner label
                animateWinnerLabel(lblWinner);
            }

            if (lblScore1 != null) {
                lblScore1.setOpacity(0);
                animateScoreLabel(lblScore1, score1, 0.5);
            }

            if (lblScore2 != null) {
                lblScore2.setOpacity(0);
                animateScoreLabel(lblScore2, score2, 0.8);
            }

            // Hiển thị WIN/LOSE cho mỗi người
            if (lblResultP1 != null) {
                String resultP1 = winner.equals("P1") ? "✓ WINNER" : (winner.equals("DRAW") ? "= DRAW" : "✗ LOSER");
                lblResultP1.setText(resultP1);
                lblResultP1.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " +
                        (winner.equals("P1") ? "#7CFC00" : winner.equals("DRAW") ? "#fbbf24" : "#ef4444") + ";");
            }

            if (lblResultP2 != null) {
                String resultP2 = winner.equals("P2") ? "✓ WINNER" : (winner.equals("DRAW") ? "= DRAW" : "✗ LOSER");
                lblResultP2.setText(resultP2);
                lblResultP2.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " +
                        (winner.equals("P2") ? "#00BFFF" : winner.equals("DRAW") ? "#fbbf24" : "#ef4444") + ";");
            }

            // Animation cho buttons
            if (btnPlayAgain != null) {
                animateButton(btnPlayAgain, 1.5);
            }
            if (btnMainMenu != null) {
                animateButton(btnMainMenu, 1.8);
            }
        });
    }

    private void animateWinnerLabel(Label label) {
        // Scale pulse animation
        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.8), label);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);

        // Fade in
        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), label);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();

        // Continuous glow effect
        parallel.setOnFinished(e -> {
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), label);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();
        });
    }

    private void animateScoreLabel(Label label, int score, double delay) {
        PauseTransition pause = new PauseTransition(Duration.seconds(delay));
        pause.setOnFinished(e -> {
            // Count up animation
            Timeline timeline = new Timeline();
            final int steps = 30;
            final long stepDuration = 30;

            for (int i = 0; i <= steps; i++) {
                final int value = (int) (score * i / (double) steps);
                KeyFrame keyFrame = new KeyFrame(
                        Duration.millis(i * stepDuration),
                        ev -> label.setText(String.valueOf(value))
                );
                timeline.getKeyFrames().add(keyFrame);
            }

            // Fade in
            FadeTransition fade = new FadeTransition(Duration.seconds(0.5), label);
            fade.setFromValue(0);
            fade.setToValue(1);

            ParallelTransition parallel = new ParallelTransition(timeline, fade);
            parallel.play();
        });
        pause.play();
    }

    private void animateButton(Button button, double delay) {
        button.setOpacity(0);
        button.setScaleX(0.8);
        button.setScaleY(0.8);

        PauseTransition pause = new PauseTransition(Duration.seconds(delay));
        pause.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.seconds(0.4), button);
            fade.setFromValue(0);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(0.4), button);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1.0);
            scale.setToY(1.0);

            ParallelTransition parallel = new ParallelTransition(fade, scale);
            parallel.play();
        });
        pause.play();
    }

    private void playAgain() {
        try {
            VersusController.stopGameLoopIfAny();

            Stage stage = (Stage) btnPlayAgain.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader();
            java.net.URL resourceUrl = getClass().getResource("/ui/fxml/VersusView.fxml");

            if (resourceUrl != null) {
                loader.setLocation(resourceUrl);
            } else {
                java.io.File fxmlFile = new java.io.File("src/arkanoid/ui/fxml/VersusView.fxml");
                loader.setLocation(fxmlFile.toURI().toURL());
            }

            Parent root = loader.load();
            stage.setScene(new Scene(root, 1280, 720));

            System.out.println("✅ Restarted versus game");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToMainMenu() {
        try {
            Stage stage = (Stage) btnMainMenu.getScene().getWindow();

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}