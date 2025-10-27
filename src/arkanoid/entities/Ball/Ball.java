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
 * Includes trail/glow effect and direction selector
 */
public class Ball extends MovableObject {

    private static final double DEFAULT_SIZE = 30;
    private static final double DEFAULT_SPEED = 325;

    // Trail effect constants
    private static final int MAX_TRAIL_POINTS = 30;
    private static final double TRAIL_UPDATE_INTERVAL = 0.01;

    // Direction selector
    private double selectedAngle = -90; // Mặc định hướng lên (độ)
    private static final double ANGLE_STEP = 15; // Mỗi lần bấm thay đổi 15 độ

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
        this.directionX = 1;
        this.directionY = -1;
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
        selectedAngle = -90; // Reset góc về hướng lên
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
     * Thay đổi góc chọn (khi bấm A/D hoặc mũi tên)
     */
    public void rotateDirection(boolean clockwise) {
        if (stuckToPaddle) {
            if (clockwise) {
                selectedAngle += ANGLE_STEP;
            } else {
                selectedAngle -= ANGLE_STEP;
            }
            // Giới hạn góc từ -170 đến -10 độ (phạm vi hợp lý)
            if (selectedAngle > -10) selectedAngle = -10;
            if (selectedAngle < -170) selectedAngle = -170;
        }
    }

    /**
     * Áp dụng góc chọn và phóng bóng
     */
    public void applySelectedDirection() {
        double radians = Math.toRadians(selectedAngle);
        directionX = Math.cos(radians);
        directionY = Math.sin(radians);
        updateVelocity();
    }

    /**
     * Vẽ mũi tên hướng dẫn trên bóng
     */
    /**
     * Vẽ mũi tên hướng dẫn trên bóng - Galaxy theme
     */
    public void renderDirectionArrow(GraphicsContext gc) {
        if (!stuckToPaddle) return;

        double ballCenterX = x + width / 2;
        double ballCenterY = y + height / 2;

        // Vị trí mũi tên ở phía trên bóng
        double arrowDistance = 70;
        double radians = Math.toRadians(selectedAngle);
        double arrowX = ballCenterX + Math.cos(radians) * arrowDistance;
        double arrowY = ballCenterY + Math.sin(radians) * arrowDistance;

        // ===== ĐƯỜNG NÉT TỪ BÓNG ĐẾN MŨI TÊN - GLOW =====
        gc.save();

        // Glow effect cho đường nét
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                javafx.scene.paint.Color.web("#a855f7", 0.8),
                20,
                0.8,
                0, 0
        ));

        gc.setStroke(javafx.scene.paint.Color.web("#c084fc", 0.8));
        gc.setLineWidth(3);
        gc.strokeLine(ballCenterX, ballCenterY, arrowX, arrowY);

        // ===== VẼ MŨI TÊN TO ĐẸP =====
        double arrowSize = 22; // Mũi tên to hơn
        double angle1 = radians + Math.toRadians(150);
        double angle2 = radians - Math.toRadians(150);

        double x1 = arrowX + Math.cos(angle1) * arrowSize;
        double y1 = arrowY + Math.sin(angle1) * arrowSize;
        double x2 = arrowX + Math.cos(angle2) * arrowSize;
        double y2 = arrowY + Math.sin(angle2) * arrowSize;

        // ===== GLOW NGOÀI (Tím Galaxy) =====
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                javafx.scene.paint.Color.web("#a855f7", 0.9),
                30,
                0.9,
                0, 0
        ));

        // Fill mũi tên - Gradient Tím đến Xanh
        javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                arrowX, arrowY - 20,
                arrowX, arrowY + 20,
                false,
                javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#c084fc")),    // Tím sáng
                new javafx.scene.paint.Stop(0.5, javafx.scene.paint.Color.web("#a855f7")),  // Tím vừa
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#7c3aed"))     // Tím đậm
        );

        gc.setFill(gradient);
        gc.fillPolygon(
                new double[]{arrowX, x1, x2},
                new double[]{arrowY, y1, y2},
                3
        );

        // ===== VIỀN MŨI TÊN - XANH NEON =====
        gc.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.GAUSSIAN,
                javafx.scene.paint.Color.web("#06b6d4", 0.9),
                15,
                0.8,
                0, 0
        ));

        gc.setStroke(javafx.scene.paint.Color.web("#22d3ee", 0.95));
        gc.setLineWidth(2.5);
        gc.strokePolygon(
                new double[]{arrowX, x1, x2},
                new double[]{arrowY, y1, y2},
                3
        );

        // ===== STAR EFFECT XUNG QUANH MŨI TÊN =====
        gc.setEffect(null);
        for (int i = 0; i < 6; i++) {
            double starAngle = Math.toRadians(i * 60);
            double starDist = 35;
            double starX = arrowX + Math.cos(starAngle) * starDist;
            double starY = arrowY + Math.sin(starAngle) * starDist;
            double starSize = 4 + Math.random() * 2;

            gc.setFill(javafx.scene.paint.Color.web("#fbbf24", 0.7 + Math.random() * 0.3));
            gc.fillOval(starX - starSize/2, starY - starSize/2, starSize, starSize);
        }

        // ===== HINT TEXT PHÍ DƯỚI =====
        gc.setEffect(null);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 13));
        gc.setFill(javafx.scene.paint.Color.web("#60a5fa", 0.85));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

        String hintText = "A/D to aim • SPACE to fire";
        gc.fillText(hintText, ballCenterX, ballCenterY + 85);

        gc.restore();
    }

    private void updateTrail() {
        timeSinceLastTrail += 0.016;

        if (timeSinceLastTrail >= TRAIL_UPDATE_INTERVAL && !stuckToPaddle) {
            double ballCenterX = x + width / 2;
            double ballCenterY = y + height / 2;

            trail.add(new TrailPoint(ballCenterX, ballCenterY, 1.0));

            if (trail.size() > MAX_TRAIL_POINTS) {
                trail.poll();
            }

            timeSinceLastTrail = 0;
        }

        for (TrailPoint point : trail) {
            point.alpha -= 0.05;
        }

        trail.removeIf(point -> point.alpha <= 0);
    }

    private void renderTrail(GraphicsContext gc) {
        for (TrailPoint point : trail) {
            if (point.alpha > 0) {
                double radius = (width / 2) * point.alpha;

                gc.setFill(Color.color(1.0, 1.0, 1.0, 0.3 * point.alpha));
                gc.fillOval(point.x - radius * 1.5, point.y - radius * 1.5,
                        radius * 3, radius * 3);

                gc.setFill(Color.color(0.6, 0.8, 1.0, 0.5 * point.alpha));
                gc.fillOval(point.x - radius, point.y - radius,
                        radius * 2, radius * 2);

                gc.setFill(Color.color(1.0, 1.0, 1.0, 0.8 * point.alpha));
                gc.fillOval(point.x - radius * 0.6, point.y - radius * 0.6,
                        radius * 1.2, radius * 1.2);
            }
        }
    }

    public void bounceOff(GameObject other) {
        double otherX = other.getX();
        double otherY = other.getY();
        double otherW = other.getWidth();
        double otherH = other.getHeight();

        double overlapLeft   = (otherX + otherW) - x;
        double overlapRight  = (x + width) - otherX;
        double overlapTop    = (otherY + otherH) - y;
        double overlapBottom = (y + height) - otherY;

        double minX = Math.min(overlapLeft, overlapRight);
        double minY = Math.min(overlapTop, overlapBottom);

        double nx = 0, ny = 0;
        double epsilon = 2.0;
        double threshold = 3.0;

        if (minX < minY - threshold) {
            if (overlapLeft < overlapRight) {
                nx = -1;
                x = otherX + otherW + epsilon;
            } else {
                nx = 1;
                x = otherX - width - epsilon;
            }
        } else if (minY < minX - threshold) {
            if (overlapTop < overlapBottom) {
                ny = -1;
                y = otherY + otherH + epsilon;
            } else {
                ny = 1;
                y = otherY - height - epsilon;
            }
        } else {
            if (Math.abs(dy) > Math.abs(dx)) {
                if (overlapTop < overlapBottom) {
                    ny = -1;
                    y = otherY + otherH + epsilon;
                } else {
                    ny = 1;
                    y = otherY - height - epsilon;
                }
            } else {
                if (overlapLeft < overlapRight) {
                    nx = -1;
                    x = otherX + otherW + epsilon;
                } else {
                    nx = 1;
                    x = otherX - width - epsilon;
                }
            }
        }

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

        if (Math.abs(directionY) < 0.2) {
            directionY = Math.copySign(0.2, directionY == 0 ? -1 : directionY);
            double m2 = Math.sqrt(directionX * directionX + directionY * directionY);
            directionX /= m2; directionY /= m2;
        }

        if (Math.abs(directionX) < 0.2) {
            directionX = Math.copySign(0.2, directionX == 0 ? 1 : directionX);
            double m2 = Math.sqrt(directionX * directionX + directionY * directionY);
            directionX /= m2; directionY /= m2;
        }

        updateVelocity();
    }

    public void bounceOffPaddle(Paddle paddle) {
        double ballCenter = x + width / 2;
        double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
        double hitPosition = (ballCenter - paddleCenter) / (paddle.getWidth() / 2);

        hitPosition = Math.max(-1, Math.min(1, hitPosition));

        directionX = hitPosition * 0.8;
        directionY = -Math.abs(directionY);

        double magnitude = Math.sqrt(directionX * directionX + directionY * directionY);
        directionX /= magnitude;
        directionY /= magnitude;

        updateVelocity();
    }

    public void bounceOffWall(char wall) {
        switch (wall) {
            case 'L':
                directionX = Math.abs(directionX);
                break;
            case 'R':
                directionX = -Math.abs(directionX);
                break;
            case 'T':
                directionY = Math.abs(directionY);
                break;
            case 'B':
                directionY = -Math.abs(directionY);
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
        renderTrail(gc);

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
    public double getSelectedAngle() { return selectedAngle; }

    private static class TrailPoint {
        double x, y, alpha;

        TrailPoint(double x, double y, double alpha) {
            this.x = x;
            this.y = y;
            this.alpha = alpha;
        }
    }
}