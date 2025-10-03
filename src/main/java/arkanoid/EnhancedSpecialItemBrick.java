package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Special item brick inspired by cute My Talking Tom Angela themes.
 * Renders a themed icon and gives higher points.
 */
public class EnhancedSpecialItemBrick extends EnhancedBrick {
    public enum ItemType { HEART, DIAMOND, BOW }

    private final ItemType itemType;

    public EnhancedSpecialItemBrick(double x, double y, double width, double height, ItemType itemType, ParticleSystem ps) {
        super(x, y, width, height, 1, BrickType.SPECIAL, ps);
        this.itemType = itemType;
        updateColor();
        // Softer shadow for cute look
        setShadow(2, 2, Color.color(0, 0, 0, 0.25));
    }

    @Override
    protected void updateColor() {
    }

    @Override
    protected void renderHighlight(GraphicsContext gc) {
    }

    private void drawHeart(GraphicsContext gc) {
    }

    private void drawDiamond(GraphicsContext gc) {
    }

    private void drawBow(GraphicsContext gc) {
    }

    @Override
    public int getPoints() {return 0;}
}
