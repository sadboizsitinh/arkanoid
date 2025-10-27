package arkanoid.entities.Ball;

import arkanoid.entities.GameObject;
import arkanoid.entities.MovableObject;
import arkanoid.entities.Paddle.Paddle;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Game ball that bounces around the screen
 * Handles collisions with other objects
 * Includes trail/glow effect
 */
public class Ball extends MovableObject {



    private static final double DEFAULT_SIZE = 30;
    private static final double DEFAULT_SPEED = 325;

    // Trail effect constants
    private static final int MAX_TRAIL_POINTS = 30;
    private static final double TRAIL_UPDATE_INTERVAL = 0.01; // Update trail every 10ms

    // Index Skin
    private int TypeSkin = 1;

    private double directionX, directionY;

    private boolean stuckToPaddle = true;
    private double offsetFromPaddleCenter = 0;

    // Trail effect
    private Queue<TrailPoint> trail;
    private double timeSinceLastTrail = 0;

    public Ball(double x, double y) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SPEED);
        this.color = Color.RED;
        // Start moving up and right
        this.directionX = 1;
        this.directionY = -1;

        // Initialize trail
        this.trail = new LinkedList<>();

        updateVelocity();
    }

    private void updateVelocity() {
        dx = directionX * speed;
        dy = directionY * speed;
    }

    public void release() {
        stuckToPaddle = false;
    }

    public void stickToPaddle(Paddle paddle) {
        stuckToPaddle = true;
        offsetFromPaddleCenter = 0;
        // Clear trail when stuck
        trail.clear();
    }

    public void updateStuckPosition(Paddle paddle) {
        if (stuckToPaddle) {
            double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
            x = paddleCenter + offsetFromPaddleCenter - width / 2;
            y = paddle.getY() - height - 2;
        }
    }

    public boolean isStuckToPaddle() {
        return stuckToPaddle;
    }

    /**
     * Update trail points - add new point if enough time has passed
     */
    private void updateTrail() {
        timeSinceLastTrail += 0.016; // Approximate delta time

        if (timeSinceLastTrail >= TRAIL_UPDATE_INTERVAL && !stuckToPaddle) {
            double ballCenterX = x + width / 2;
            double ballCenterY = y + height / 2;

            trail.add(new TrailPoint(ballCenterX, ballCenterY, 1.0));

            // Keep trail size limited
            if (trail.size() > MAX_TRAIL_POINTS) {
                trail.poll();
            }

            timeSinceLastTrail = 0;
        }

        // Fade out trail points
        for (TrailPoint point : trail) {
            point.alpha -= 0.05;
        }

        // Remove fully faded points
        trail.removeIf(point -> point.alpha <= 0);
    }

    /**
     * Render trail effect
     */
    private void renderTrail(GraphicsContext gc) {
        for (TrailPoint point : trail) {
            if (point.alpha > 0) {
                // Draw glowing circle with decreasing size and opacity
                double radius = (width / 2) * point.alpha;

                // Outer glow - more transparent
                gc.setFill(Color.color(1.0, 1.0, 1.0, 0.3 * point.alpha));
                gc.fillOval(point.x - radius * 1.5, point.y - radius * 1.5,
                        radius * 3, radius * 3);

                // Middle glow
                gc.setFill(Color.color(0.6, 0.8, 1.0, 0.5 * point.alpha));
                gc.fillOval(point.x - radius, point.y - radius,
                        radius * 2, radius * 2);

                // Core - brighter
                gc.setFill(Color.color(1.0, 1.0, 1.0, 0.8 * point.alpha));
                gc.fillOval(point.x - radius * 0.6, point.y - radius * 0.6,
                        radius * 1.2, radius * 1.2);
            }
        }
    }

    /**
     * Bounce off another object using axis penetration to determine normal,
     * then apply specular reflection.
     * FIXED: Better corner detection + prevent double collision
     */
    public void bounceOff(GameObject other) {
        double otherX = other.getX();
        double otherY = other.getY();
        double otherW = other.getWidth();
        double otherH = other.getHeight();

        // Tính overlap theo 4 hướng
        double overlapLeft   = (otherX + otherW) - x;
        double overlapRight  = (x + width) - otherX;
        double overlapTop    = (otherY + otherH) - y;
        double overlapBottom = (y + height) - otherY;

        // Tìm overlap NHỎ NHẤT (= hướng va chạm thực tế)
        double minX = Math.min(overlapLeft, overlapRight);
        double minY = Math.min(overlapTop, overlapBottom);

        double nx = 0, ny = 0;
        double epsilon = 2.0;

        // THRESHOLD: Nếu chênh lệch quá nhỏ = va chạm góc → ưu tiên trục Y
        double threshold = 3.0; // pixels

        if (minX < minY - threshold) {
            // Va chạm rõ ràng từ TRÁI/PHẢI
            if (overlapLeft < overlapRight) {
                nx = -1;
                x = otherX + otherW + epsilon;
            } else {
                nx = 1;
                x = otherX - width - epsilon;
            }
        } else if (minY < minX - threshold) {
            // Va chạm rõ ràng từ TRÊN/DƯỚI
            if (overlapTop < overlapBottom) {
                ny = -1;
                y = otherY + otherH + epsilon;
            } else {
                ny = 1;
                y = otherY - height - epsilon;
            }
        } else {
            // VA CHẠM GÓC: Ưu tiên hướng di chuyển hiện tại
            if (Math.abs(dy) > Math.abs(dx)) {
                // Bóng đang đi chủ yếu theo trục Y → bounce theo Y
                if (overlapTop < overlapBottom) {
                    ny = -1;
                    y = otherY + otherH + epsilon;
                } else {
                    ny = 1;
                    y = otherY - height - epsilon;
                }
            } else {
                // Bóng đang đi chủ yếu theo trục X → bounce theo X
                if (overlapLeft < overlapRight) {
                    nx = -1;
                    x = otherX + otherW + epsilon;
                } else {
                    nx = 1;
                    x = otherX - width - epsilon;
                }
            }
        }

        // Specular reflection
        double vx = dx, vy = dy;
        double dot = vx * nx + vy * ny;
        double rx = vx - 2 * dot * nx;
        double ry = vy - 2 * dot * ny;

        double mag = Math.sqrt(rx * rx + ry * ry);
        if (mag == 0) {
            rx = -vx; ry = -vy; mag = Math.sqrt(rx * rx + ry * ry);
        }
        directionX = rx / mag;
        directionY = ry / mag;

        // Prevent too horizontal OR too vertical movement
        if (Math.abs(directionY) < 0.2) {
            directionY = Math.copySign(0.2, directionY == 0 ? -1 : directionY);
            double m2 = Math.sqrt(directionX * directionX + directionY * directionY);
            directionX /= m2; directionY /= m2;
        }

        // THÊM: Prevent too vertical movement (tránh bay thẳng đứng)
        if (Math.abs(directionX) < 0.2) {
            directionX = Math.copySign(0.2, directionX == 0 ? 1 : directionX);
            double m2 = Math.sqrt(directionX * directionX + directionY * directionY);
            directionX /= m2; directionY /= m2;
        }

        updateVelocity();
    }

    /**
     * Bounce off paddle with angle variation
     * NO position adjustment - just reflect immediately
     */
    public void bounceOffPaddle(Paddle paddle) {
        // KHÔNG ĐẨY VỊ TRÍ - chỉ đổi hướng ngay lập tức

        double ballCenter = x + width / 2;
        double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
        double hitPosition = (ballCenter - paddleCenter) / (paddle.getWidth() / 2);

        // Clamp hitPosition trong khoảng [-1, 1]
        hitPosition = Math.max(-1, Math.min(1, hitPosition));

        directionX = hitPosition * 0.8;
        directionY = -Math.abs(directionY);  // Luôn bay lên

        double magnitude = Math.sqrt(directionX * directionX + directionY * directionY);
        directionX /= magnitude;
        directionY /= magnitude;

        updateVelocity();
    }

    /**
     * Bounce off wall - Simple reflection
     */
    public void bounceOffWall(char wall) {
        switch (wall) {
            case 'L':
                directionX = Math.abs(directionX);  // Force right
                break;
            case 'R':
                directionX = -Math.abs(directionX); // Force left
                break;
            case 'T':
                directionY = Math.abs(directionY);  // Force down
                break;
            case 'B':
                directionY = -Math.abs(directionY); // Force up
                break;
        }
        updateVelocity();
    }

    public void setTypeSkin(int type) {
        TypeSkin = type;
    }

    public int getTypeSkin() {
        return TypeSkin;
    }

    public void applySpeed(double newSpeed) {
        this.speed = newSpeed;
        updateVelocity();
    }

    @Override
    public void update(double deltaTime) {
        if (!stuckToPaddle) {
            move(deltaTime);
            updateTrail();
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // Render trail first (behind the ball)
        renderTrail(gc);

        // Render ball
        String path = "file:src/arkanoid/assets/images/skinball_" + TypeSkin + ".png";
        loadTexture(path);

        setSpriteRegion(0, 0, 317, 323);

        gc.drawImage(
                spriteSheet,
                sourceX, sourceY, sourceWidth, sourceHeight,
                x, y, width, height
        );
    }

    public void setDirection(double dx, double dy) {
        this.directionX = dx;
        this.directionY = dy;
        updateVelocity();
    }

    public double getSpeed() { return speed; }
    public double getDX() { return dx; }
    public double getDY() { return dy; }

    /**
     * Inner class to represent a trail point
     */
    private static class TrailPoint {
        double x, y, alpha;

        TrailPoint(double x, double y, double alpha) {
            this.x = x;
            this.y = y;
            this.alpha = alpha;
        }
    }
}