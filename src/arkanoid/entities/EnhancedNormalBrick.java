package arkanoid.entities;

import arkanoid.effects.ParticleSystem;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Enhanced normal brick with gradient and effects
 */
public class EnhancedNormalBrick extends EnhancedBrick {

    public EnhancedNormalBrick(double x, double y, double width, double height, ParticleSystem particleSystem) {
        super(x, y, width, height, 1, BrickType.NORMAL, particleSystem);

        // Set gradient colors
        Color baseColor = Color.color(1.0, 0.6, 0.2); // Orange
        setGradient(baseColor.brighter(), baseColor.darker());
        this.color = baseColor;
    }

    @Override
    protected void updateColor() {
    }
}
