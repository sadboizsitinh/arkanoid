package arkanoid.entities.Brick;

import arkanoid.entities.GameObject;
import javafx.scene.canvas.GraphicsContext;

/**
 * Base class for all brick types
 * Can be hit and destroyed
 */
public abstract class Brick extends GameObject {
    protected int hitPoints;
    protected int maxHitPoints;
    protected boolean destroyed;
    protected BrickType type;

    public enum BrickType {
        NORMAL, STRONG, UNBREAKABLE
    }

    public Brick(double x, double y, double width, double height, int hitPoints, BrickType type) {
        super(x, y, width, height);
        this.hitPoints = hitPoints;
        this.maxHitPoints = hitPoints;
        this.destroyed = false;
        this.type = type;
    }

    /**
     * Handle being hit by the ball
     */
    public void takeHit() {
        if (type != BrickType.UNBREAKABLE) {
            hitPoints--;
            if (hitPoints <= 0) {
                destroyed = true;

            }
            updateColor();
        }
    }

    public abstract void updateColor();

    public boolean isDestroyed() {
        return destroyed;
    }

    public BrickType getType() {
        return type;
    }

    public int getPoints() {
        switch (type) {
            case NORMAL:
                return 10;
            case STRONG:
                return 20;
            case UNBREAKABLE:
                return 0;
            default:
                return 0; // fallback an toàn
        }
    }

    /**
     * (HÀM MỚI - BẮT BUỘC THÊM VÀO)
     * Dùng để khôi phục hitPoints từ file save.
     */
    public void setHitPoints(int hp) {
        this.hitPoints = hp;

        // Đảm bảo trạng thái 'destroyed' được cập nhật
        if (hp <= 0 && this.type != BrickType.UNBREAKABLE) {
            this.destroyed = true;
        }

        // Cập nhật lại màu sắc (quan trọng cho StrongBrick)
        updateColor();
    }

    /**
     * (HÀM BỊ THIẾU - HÃY THÊM VÀO)
     * Dùng để lấy hitPoints hiện tại (để lưu game)
     */
    public int getHitPoints() {
        return this.hitPoints;
    }

    @Override
    public void update(double deltaTime) {
        // Bricks don't need to update unless animated
    }

    @Override
    public void render(GraphicsContext gc) {
        String BrickCode = "";
        int status = 1;
        if (type == BrickType.NORMAL) {
            BrickCode = "normal_";
        }
        if (type == BrickType.STRONG) {
            BrickCode = "strong_";
            status = 4 - hitPoints;
        }
        if (type == BrickType.UNBREAKABLE) {
            BrickCode = "unbreakable_";
        }
        String path = "file:src/arkanoid/assets/images/brick_" + BrickCode + status + ".png";
        loadTexture(path);



        setSpriteRegion(0,0,735, 210);
        gc.drawImage(
                spriteSheet,
                sourceX, sourceY, sourceWidth, sourceHeight,
                x, y, width, height
        );
    }

}