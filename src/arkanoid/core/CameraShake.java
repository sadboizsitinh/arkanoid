package arkanoid.core;

/**
 * Camera shake effect manager
 * Provides screen vibration effects for collisions
 */
public class CameraShake {
    private double shakeIntensity = 0;
    private double maxShakeIntensity = 0;
    private double shakeDuration = 0;
    private double maxShakeDuration = 0;
    private double shakeX = 0;
    private double shakeY = 0;

    // Constants for different shake types
    private static final double BRICK_SHAKE_INTENSITY = 4.0;
    private static final double BRICK_SHAKE_DURATION = 0.2; // 100ms

    private static final double PADDLE_SHAKE_INTENSITY = 2.0;
    private static final double PADDLE_SHAKE_DURATION = 0.09; // 60ms

    public CameraShake() {
        this.shakeIntensity = 0;
        this.shakeDuration = 0;
    }

    /**
     * Trigger brick collision shake (stronger)
     */
    public void shakeOnBrickHit() {
        startShake(BRICK_SHAKE_INTENSITY, BRICK_SHAKE_DURATION);
    }

    /**
     * Trigger paddle collision shake (lighter)
     */
    public void shakeOnPaddleHit() {
        startShake(PADDLE_SHAKE_INTENSITY, PADDLE_SHAKE_DURATION);
    }

    /**
     * Start a custom shake
     */
    private void startShake(double intensity, double duration) {
        // If already shaking, only update if new shake is stronger
        if (shakeIntensity < intensity) {
            this.maxShakeIntensity = intensity;
            this.shakeIntensity = intensity;
            this.maxShakeDuration = duration;
            this.shakeDuration = duration;
        }
    }

    /**
     * Update shake effect (call every frame)
     */
    public void update(double deltaTime) {
        if (shakeDuration > 0) {
            shakeDuration -= deltaTime;

            // Fade out shake intensity
            shakeIntensity = (shakeDuration / maxShakeDuration) * maxShakeIntensity;

            // Generate random offset
            shakeX = (Math.random() - 0.5) * shakeIntensity * 2;
            shakeY = (Math.random() - 0.5) * shakeIntensity * 2;
        } else {
            shakeIntensity = 0;
            shakeX = 0;
            shakeY = 0;
        }
    }

    /**
     * Get current X offset to apply to camera/canvas
     */
    public double getShakeX() {
        return shakeX;
    }

    /**
     * Get current Y offset to apply to camera/canvas
     */
    public double getShakeY() {
        return shakeY;
    }

    /**
     * Check if shake is active
     */
    public boolean isShaking() {
        return shakeIntensity > 0;
    }
}