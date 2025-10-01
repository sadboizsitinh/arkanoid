package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Enhanced unbreakable brick with special visual effects
 */
public class EnhancedUnbreakableBrick extends EnhancedBrick {
    private double pulseIntensity;

    public EnhancedUnbreakableBrick(double x, double y, double width, double height, ParticleSystem particleSystem) {
        super(x, y, width, height, Integer.MAX_VALUE, BrickType.UNBREAKABLE, particleSystem);

        // Set metallic appearance
        Color baseColor = Color.color(0.7, 0.7, 0.8); // Steel gray
        setGradient(baseColor.brighter(), baseColor.darker());
        this.color = baseColor;
        this.pulseIntensity = 0;
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    protected void updateColor() {
    }

    @Override
    public void takeHit() {
    }

    @Override
    protected void renderHighlight(GraphicsContext gc) {
    }
}
