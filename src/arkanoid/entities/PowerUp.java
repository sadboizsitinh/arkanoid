package arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all power-ups
 * Falls down from destroyed bricks
 */
public abstract class PowerUp extends MovableObject {
    protected PowerUpType type;
    protected double duration;
    protected double timeRemaining;
    protected boolean active;

    // ✅ Cache images để tránh load lại nhiều lần
    private static final Map<PowerUpType, Image> imageCache = new HashMap<>();
    private static boolean useImages = true; // ✅ Toggle để bật/tắt image mode

    public enum PowerUpType {
        EXPAND_PADDLE, FAST_BALL, MULTI_BALL, EXTRA_LIFE, SLOW_BALL
    }

    public PowerUp(double x, double y, PowerUpType type, double duration) {
        super(x, y, 30, 30, 100); // Size 30x30 cho image
        this.type = type;
        this.duration = duration;
        this.timeRemaining = duration;
        this.active = false;
        this.dy = speed; // Fall down

        // ✅ Load image khi khởi tạo (chỉ load 1 lần cho mỗi type)
        if (useImages) {
            loadImage();
        }
    }

    /**
     * Load image từ resources (chỉ load 1 lần cho mỗi PowerUpType)
     */
    private void loadImage() {
        if (!imageCache.containsKey(type)) {
            try {
                String imagePath = getImagePath();
                java.net.URL url = getClass().getResource(imagePath);

                if (url != null) {
                    Image img = new Image(url.toString());
                    imageCache.put(type, img);
                    System.out.println("✅ Loaded power-up image: " + imagePath);
                } else {
                    System.err.println("⚠️ Image not found: " + imagePath + " - Using text fallback");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error loading image: " + e.getMessage() + " - Using text fallback");
            }
        }
    }

    /**
     * Trả về đường dẫn image theo type
     */
    protected String getImagePath() {
        switch (type) {
            case EXPAND_PADDLE:
                return "/arkanoid/assets/images/expand_paddle.png";
            case FAST_BALL:
                return "/arkanoid/assets/images/fast_ball.png";
            case SLOW_BALL:
                return "/arkanoid/assets/images/slow_ball.png";
            case MULTI_BALL:
                return "/arkanoid/assets/images/multi_ball.png";
            case EXTRA_LIFE:
                return "/arkanoid/assets/images/extra_life.png";
            default:
                return "/arkanoid/assets/images/default.png";
        }
    }

    @Override
    public void update(double deltaTime) {
        if (!active) {
            move(deltaTime); // Fall down
        } else {
            timeRemaining -= deltaTime;
        }
    }

    public boolean isExpired() {
        return active && timeRemaining <= 0;
    }

    public void activate() {
        active = true;
        timeRemaining = duration;
    }

    public abstract void applyEffect(PaddleLike paddle);
    public abstract void removeEffect(PaddleLike paddle);

    @Override
    public void render(GraphicsContext gc) {
        Image img = useImages ? imageCache.get(type) : null;

        if (img != null && !img.isError()) {
            // ✅ MODE 1: Render với IMAGE
            gc.drawImage(img, x, y, 80, 60);

            // Optional: Thêm border cho đẹp
           // gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
          //  gc.strokeRect(x, y, width, height);
        } else {
            // ✅ MODE 2: Render với TEXT (fallback hoặc khi không có image)
            // Background màu
            gc.setFill(color);
            gc.fillRect(x, y, width, height);

            // Border
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);

            // Symbol text từ class con (F, S, M, E, L...)
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 18));

            String symbol = getSymbol();
            // Center text
            double textWidth = symbol.length() * 10; // Ước lượng
            gc.fillText(symbol, x + (width - textWidth) / 2, y + height / 2 + 6);
        }
    }

    /**
     * Method abstract - các class con sẽ override
     * VD: FastBallPowerUp return "F", SlowBallPowerUp return "S"...
     */
    protected abstract String getSymbol();

    public PowerUpType getType() {
        return type;
    }

    public double getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(double time) {
        this.timeRemaining = time;
    }

    public double getDuration() {
        return duration;
    }

    /**
     * Dùng cho UI hiển thị power-up đang active
     */
    public String getDisplaySymbol() {
        return getSymbol();
    }

    /**
     * ✅ Static method để toggle giữa image mode và text mode
     */
    public static void setUseImages(boolean use) {
        useImages = use;
    }
}