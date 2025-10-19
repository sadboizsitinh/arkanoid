package arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Base class for all power-ups
 * Falls down from destroyed bricks
 */
public abstract class PowerUp extends MovableObject {
    protected PowerUpType type;
    protected double duration;
    protected double timeRemaining;
    protected boolean active;

    public enum PowerUpType {
        EXPAND_PADDLE, FAST_BALL, MULTI_BALL, EXTRA_LIFE, SLOW_BALL
    }

    public PowerUp(double x, double y, PowerUpType type, double duration) {
        super(x, y, 20, 20, 100); // Standard size and fall speed
        this.type = type;
        this.duration = duration;
        this.timeRemaining = duration;
        this.active = false;
        this.dy = speed; // Fall down
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
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, width, height);

        // Draw power-up symbol
        gc.setFill(Color.BLACK);
        gc.fillText(getSymbol(), x + 5, y + 15);
    }

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

    public String getDisplaySymbol() {
        return getSymbol();
    }


}
