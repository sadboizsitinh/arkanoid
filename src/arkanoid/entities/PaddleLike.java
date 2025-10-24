package arkanoid.entities;

/**
 * Common interface for paddle-like objects used by ball collision and power-ups.
 */
public interface PaddleLike {
    double getX();
    double getY();
    double getWidth();
    double getHeight();
    void setWidth(double width);
    void setHeight(double height);
    void resetSize();
}
