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

    // Cache images để tránh load lại nhiều lần
    private static final Map<PowerUpType, Image> imageCache = new HashMap<>();
    private static boolean useImages = true;

    // Kích thước chuẩn để hiển thị
    private static final double DISPLAY_WIDTH = 60;
    private static final double DISPLAY_HEIGHT = 60;

    public enum PowerUpType {
        EXPAND_PADDLE, FAST_BALL, MULTI_BALL, EXTRA_LIFE, SLOW_BALL
    }

    public PowerUp(double x, double y, PowerUpType type, double duration) {
        super(x, y, DISPLAY_WIDTH, DISPLAY_HEIGHT, 100);
        this.type = type;
        this.duration = duration;
        this.timeRemaining = duration;
        this.active = false;
        this.dy = speed; // Fall down

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
            // MODE 1: Render với IMAGE (scale theo tỷ lệ gốc)
            renderImageWithAspectRatio(gc, img);
        } else {
            // MODE 2: Render với TEXT (fallback hoặc khi không có image)
            renderTextFallback(gc);
        }
    }

    /**
     * Render image với tỷ lệ khung hình gốc
     */
    private void renderImageWithAspectRatio(GraphicsContext gc, Image img) {
        if (img == null || img.isError()) return;

        double imgWidth = img.getWidth();
        double imgHeight = img.getHeight();

        // Tính tỷ lệ gốc
        double aspectRatio = imgWidth / imgHeight;

        // Luôn scale ảnh để lấp đầy toàn bộ kích thước 60x60
        // mà vẫn giữ tỷ lệ khung hình gốc (crop hoặc letterbox)
        double displayWidth, displayHeight;
        double offsetX, offsetY;

        if (aspectRatio >= 1) {
            // Ảnh ngang - fit theo chiều cao
            displayHeight = DISPLAY_HEIGHT;
            displayWidth = DISPLAY_HEIGHT * aspectRatio;
            offsetX = x + (DISPLAY_WIDTH - displayWidth) / 2;
            offsetY = y;
        } else {
            // Ảnh dọc - fit theo chiều rộng
            displayWidth = DISPLAY_WIDTH;
            displayHeight = DISPLAY_WIDTH / aspectRatio;
            offsetX = x;
            offsetY = y + (DISPLAY_HEIGHT - displayHeight) / 2;
        }

        // Vẽ ảnh với kích thước đồng nhất
        gc.drawImage(img, offsetX, offsetY, displayWidth, displayHeight);
    }

    /**
     * Render text fallback khi không có image
     */
    private void renderTextFallback(GraphicsContext gc) {
        // Background màu theo type
        gc.setFill(getTypeColor());
        gc.fillRect(x, y, DISPLAY_WIDTH, DISPLAY_HEIGHT);

        // Border
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, DISPLAY_WIDTH, DISPLAY_HEIGHT);

        // Symbol text
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 24));

        String symbol = getSymbol();
        // Center text
        double textWidth = symbol.length() * 14;
        gc.fillText(symbol, x + (DISPLAY_WIDTH - textWidth) / 2, y + DISPLAY_HEIGHT / 2 + 8);
    }

    /**
     * Trả về màu theo loại power-up
     */
    private Color getTypeColor() {
        switch (type) {
            case EXPAND_PADDLE:
                return Color.web("#FF6B6B"); // Đỏ
            case FAST_BALL:
                return Color.web("#FFD93D"); // Vàng
            case SLOW_BALL:
                return Color.web("#6BCB77"); // Xanh lá
            case MULTI_BALL:
                return Color.web("#4D96FF"); // Xanh dương
            case EXTRA_LIFE:
                return Color.web("#FF69B4"); // Hồng
            default:
                return Color.GRAY;
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
     * Static method để toggle giữa image mode và text mode
     */
    public static void setUseImages(boolean use) {
        useImages = use;
    }

    /**
     * Clear cache khi cần
     */
    public static void clearImageCache() {
        imageCache.clear();
    }
}