package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

/**
 * Enhanced base class with improved visual rendering
 * Adds gradients, shadows, and smooth animations
 */
public abstract class EnhancedGameObject extends GameObject {
    protected boolean hasGradient;
    protected Color gradientStart, gradientEnd;
    protected boolean hasShadow;
    protected double shadowOffsetX, shadowOffsetY;
    protected Color shadowColor;
    protected double scale;
    protected double rotation;
    protected double pulsePhase;

    public EnhancedGameObject(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.hasGradient = false;
        this.hasShadow = true;
        this.shadowOffsetX = 2;
        this.shadowOffsetY = 2;
        this.shadowColor = Color.color(0, 0, 0, 0.3);
        this.scale = 1.0;
        this.rotation = 0;
        this.pulsePhase = 0;
    }

    /**
     * Enable gradient rendering
     */
    public void setGradient(Color start, Color end) {
    }

    /**
     * Configure shadow
     */
    public void setShadow(double offsetX, double offsetY, Color shadowColor) {
    }

    /**
     * Enhanced render method with visual effects
     */
    protected void renderEnhanced(GraphicsContext gc) {
    }

    /**
     * Override this to define the shape
     */
    protected abstract void renderShape(GraphicsContext gc, double offsetX, double offsetY);

    /**
     * Override this to add highlights
     */
    protected void renderHighlight(GraphicsContext gc) {

    }

    // Setters for animation
    public void setScale(double scale) { this.scale = scale; }
    public void setRotation(double rotation) { this.rotation = rotation; }
    public double getScale() { return scale; }
    public double getRotation() { return rotation; }
}
