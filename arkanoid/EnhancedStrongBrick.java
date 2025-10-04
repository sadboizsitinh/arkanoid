package arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Enhanced strong brick with dynamic color changes
 */
public class EnhancedStrongBrick extends EnhancedBrick {

    public EnhancedStrongBrick(double x, double y, double width, double height, ParticleSystem particleSystem) {
        super(x, y, width, height, 3, BrickType.STRONG, particleSystem);

        updateColor(); // Set initial color
    }

    @Override
    protected void updateColor() {
    }

    @Override
    public void render(GraphicsContext gc) {
    }
}
