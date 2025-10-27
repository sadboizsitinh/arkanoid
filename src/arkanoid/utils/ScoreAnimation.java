package arkanoid.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class ScoreAnimation {

    // Biến đếm số lần animation đang chạy
    private static int animationCounter = 0;

    /**
     * Hiển thị +XX điểm bay lên và biến mất
     */
    public static void showFloatingScore(Pane container, double startX, double startY, int points, boolean isStreak) {
        // Tạo label hiển thị +XX
        Label scoreLabel = new Label("+" + points);

        // Font size động dựa vào số điểm - CHỈ XỬ LÝ ĐẾN +500
        int digits = String.valueOf(points).length() + 1; // +1 cho dấu "+"
        int fontSize;

        if (digits <= 3) {
            fontSize = 20; // +5 đến +99
        } else if (digits == 4) {
            fontSize = 13; // +100 đến +500
        } else {
            fontSize = 11; // +1000+
        }

        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));

        // Màu khác nhau cho Streak và bình thường
        String color = isStreak ? "#f97316" : "#10b981"; // Streak = cam, Bình thường = xanh lá
        scoreLabel.setTextFill(Color.web(color));
        scoreLabel.setOpacity(0.0);

        // Shadow màu tương ứng
        String shadowColor = isStreak ? "rgba(249, 115, 22, 0.8)" : "rgba(16, 185, 129, 0.8)";
        scoreLabel.setStyle("-fx-effect: dropshadow(gaussian, " + shadowColor + ", 8, 0.6, 0, 0);");

        // Thêm vào container ngay - chồng lên nhau
        container.getChildren().add(scoreLabel);

        // Tính delay dựa trên số animation đang chạy (mỗi cái cách nhau 150ms)
        int currentIndex = animationCounter++;
        double delay = currentIndex * 150.0; // 150ms giữa mỗi animation

        // Animation: bay lên và mờ dần với delay
        Timeline animation = new Timeline(
                // Bắt đầu với delay
                new KeyFrame(Duration.millis(delay),
                        new KeyValue(scoreLabel.translateYProperty(), 0),
                        new KeyValue(scoreLabel.opacityProperty(), 0.0)
                ),
                // Fade in nhanh
                new KeyFrame(Duration.millis(delay + 100),
                        new KeyValue(scoreLabel.translateYProperty(), 0),
                        new KeyValue(scoreLabel.opacityProperty(), 1.0)
                ),
                // Bay lên và mờ dần
                new KeyFrame(Duration.millis(delay + 900),
                        new KeyValue(scoreLabel.translateYProperty(), -50),
                        new KeyValue(scoreLabel.opacityProperty(), 0.0)
                )
        );

        // Xóa label sau khi animation xong
        animation.setOnFinished(e -> {
            container.getChildren().remove(scoreLabel);
            animationCounter--; // Giảm counter khi xong
        });

        animation.play();
    }

    /**
     * Animation số chạy nhanh từ giá trị cũ lên giá trị mới
     */
    public static void animateScoreCount(Label label, int fromScore, int toScore) {
        if (label == null) return;

        int diff = toScore - fromScore;
        double duration = Math.min(500, diff * 10);

        Timeline timeline = new Timeline();

        int steps = 20;
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            int currentScore = (int) (fromScore + (toScore - fromScore) * progress);

            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(duration * progress),
                    e -> {
                        // CHỈ update text, KHÔNG đụng style
                        label.setText(String.valueOf(currentScore));
                    }
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }

    /**
     * Hiệu ứng flash cho label khi tăng điểm
     */
    public static void flashLabel(Label label) {
        if (label == null) return;

        // LƯU style hiện tại (đã có font size mới)
        String currentStyle = label.getStyle();

        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    // GIỮ NGUYÊN font size, CHỈ đổi màu và scale
                    label.setStyle(currentStyle.replace("-fx-text-fill: white;", "-fx-text-fill: #fbbf24;"));
                    label.setScaleX(1.1);
                    label.setScaleY(1.1);
                }),
                new KeyFrame(Duration.millis(100), e -> {
                    // Restore lại màu trắng, giữ font size
                    label.setStyle(currentStyle);
                    label.setScaleX(1.0);
                    label.setScaleY(1.0);
                })
        );
        flash.play();
    }
}