package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
/**
 * Game ball that bounces around the screen
 * Handles collisions with other objects
 */
public class Ball extends MovableObject {
    private static final double DEFAULT_SIZE = 28;
    private static final double DEFAULT_SPEED = 325;

    // Index Skin
    private int TypeSkin = 2;

    private double directionX, directionY;

    // ✅ Trạng thái dính trên paddle
    private boolean stuckToPaddle = true;
    private double offsetFromPaddleCenter = 0; // Vị trí tương đối so với tâm paddle

    public Ball(double x, double y) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SPEED);
        this.color = Color.RED;
        // Start moving up and right (chỉ dùng khi release)
        this.directionX = 1;
        this.directionY = -1;

        updateVelocity();
    }

    private void updateVelocity() {
        dx = directionX * speed;
        dy = directionY * speed;
    }

    /**
     * ✅ Release ball from paddle
     */
    public void release() {
        stuckToPaddle = false;
    }

    /**
     * ✅ Stick ball to paddle at start/new level
     */
    public void stickToPaddle(Paddle paddle) {
        stuckToPaddle = true;
        // Tính offset từ tâm paddle
        double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
        double ballCenter = x + width / 2;
        offsetFromPaddleCenter = ballCenter - paddleCenter;
    }

    /**
     * ✅ Update position when stuck to paddle
     */
    public void updateStuckPosition(Paddle paddle) {
        if (stuckToPaddle) {
            double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
            x = paddleCenter + offsetFromPaddleCenter - width / 2;
            y = paddle.getY() - height - 2; // Đặt trên paddle
        }
    }

    /**
     * ✅ Check if ball is stuck
     */
    public boolean isStuckToPaddle() {
        return stuckToPaddle;
    }

    /**
     * Bounce off another object using axis penetration to determine normal,
     * then apply specular reflection. Also nudge the ball out of the object
     * and clamp vertical component to avoid horizontal stalls.
     */
    public void bounceOff(GameObject other) {
        // Compute overlaps
        double otherX = other.getX();
        double otherY = other.getY();
        double otherW = other.getWidth();
        double otherH = other.getHeight();

        double overlapLeft   = (otherX + otherW) - x;            // amount from left
        double overlapRight  = (x + width) - otherX;             // from right
        double overlapTop    = (otherY + otherH) - y;            // from top
        double overlapBottom = (y + height) - otherY;            // from bottom

        // Choose the minimum penetration axis
        double minX = Math.min(Math.abs(overlapLeft), Math.abs(overlapRight));
        double minY = Math.min(Math.abs(overlapTop), Math.abs(overlapBottom));

        double nx = 0, ny = 0; // collision normal
        double epsilon = 0.5;  // small nudge
        if (minX < minY) {
            if (Math.abs(overlapLeft) < Math.abs(overlapRight)) {
                nx = -1; // normal points left -> hit object's left side
                x = otherX + otherW + epsilon; // move to right of object
            } else {
                nx = 1;  // hit object's right side
                x = otherX - width - epsilon;  // move to left of object
            }
        } else {
            if (Math.abs(overlapTop) < Math.abs(overlapBottom)) {
                ny = -1; // hit object's top side
                y = otherY + otherH + epsilon; // push below top
            } else {
                ny = 1;  // hit object's bottom side
                y = otherY - height - epsilon; // push above bottom
            }
        }

        // Reflect current velocity vector about normal
        double vx = dx, vy = dy;
        double dot = vx * nx + vy * ny;
        double rx = vx - 2 * dot * nx;
        double ry = vy - 2 * dot * ny;

        // Normalize back to direction and keep same speed
        double mag = Math.sqrt(rx * rx + ry * ry);
        if (mag == 0) {
            rx = -vx; ry = -vy; mag = Math.sqrt(rx * rx + ry * ry);
        }
        directionX = rx / mag;
        directionY = ry / mag;

        // Prevent near-horizontal motion which can feel sticky: ensure |directionY| >= 0.2
        if (Math.abs(directionY) < 0.2) {
            directionY = Math.copySign(0.2, directionY == 0 ? -1 : directionY);
            // Re-normalize
            double m2 = Math.sqrt(directionX * directionX + directionY * directionY);
            directionX /= m2; directionY /= m2;
        }

        updateVelocity();
    }

    /**
     * Bounce off paddle with angle variation
     */
    public void bounceOffPaddle(Paddle paddle) {
        double ballCenter = x + width / 2;
        double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
        double hitPosition = (ballCenter - paddleCenter) / (paddle.getWidth() / 2);

        directionX = hitPosition * 0.8; // Max 80% horizontal component
        directionY = -Math.abs(directionY); // Always bounce up

        // Normalize direction vector
        double magnitude = Math.sqrt(directionX * directionX + directionY * directionY);
        directionX /= magnitude;
        directionY /= magnitude;

        updateVelocity();
    }

    /**
     * Overload to support EnhancedPaddle without changing class hierarchy
     */
    public void bounceOffPaddle(EnhancedPaddle paddle) {
        double ballCenter = x + width / 2;
        double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
        double hitPosition = (ballCenter - paddleCenter) / (paddle.getWidth() / 2);

        directionX = hitPosition * 0.8; // Max 80% horizontal component
        directionY = -Math.abs(directionY); // Always bounce up

        // Normalize direction vector
        double magnitude = Math.sqrt(directionX * directionX + directionY * directionY);
        directionX /= magnitude;
        directionY /= magnitude;

        updateVelocity();
    }

    public void bounceOffWall(char wall) {
        // Use specular reflection: v' = v - 2 (v·n) n
        double vx = dx; double vy = dy;
        double nx = 0, ny = 0; // surface normal
        switch (wall) {
            case 'L':
                nx = 1; ny = 0;
                break;
            case 'R':
                nx = -1; ny = 0;
                break;
            case 'T':
                nx = 0; ny = 1;
                break;
            default:
                nx = 0; ny = -1;
                break;
        }

        double dot = vx * nx + vy * ny;
        double rx = vx - 2 * dot * nx;
        double ry = vy - 2 * dot * ny;
        // Normalize to direction and keep same speed
        double mag = Math.sqrt(rx * rx + ry * ry);
        if (mag == 0) { rx = -vx; ry = -vy; mag = Math.sqrt(rx*rx + ry*ry); }
        directionX = rx / mag;
        directionY = ry / mag;
        updateVelocity();
    }

    public void setTypeSkin (int type) {
        TypeSkin = type;
    }

    public int getTypeSkin () {
        return TypeSkin;
    }

    /**
     * Safely apply a new speed while preserving current direction.
     * Updates dx, dy to reflect the new speed.
     */
    public void applySpeed(double newSpeed) {
        this.speed = newSpeed;
        updateVelocity();
    }

    @Override
    public void update(double deltaTime) {
        // ✅ Chỉ di chuyển khi không dính trên paddle
        if (!stuckToPaddle) {
            move(deltaTime);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        String path = "/skinball_" + TypeSkin + ".png";
        loadTexture(path);

        Image img = new Image(path);
        double W = img.getWidth();
        double H = img.getHeight();

        setSpriteRegion(0,0,W, H);
        gc.drawImage(
                spriteSheet,
                sourceX, sourceY, sourceWidth, sourceHeight,  // Source rectangle
                x, y, width, height                            // Destination rectangle
        );
    }

    public void setDirection(double dx, double dy) {
        this.directionX = dx;
        this.directionY = dy;
        updateVelocity();
    }
    // --- Added getters to support snapshot/resume ---
    public double getSpeed() { return speed; }
    public double getDX() { return dx; }
    public double getDY() { return dy; }
}