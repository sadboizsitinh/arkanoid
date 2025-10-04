package arkanoid;

/**
 * Sản xuất gạch bất kỳ và tạo gạch random
 */
public class BrickFactory {
    // Tạo loại gạch bất kỳ
    public static Brick createBrick(Brick.BrickType type, double x, double y, double width, double height) {
        switch (type) {
            case NORMAL :
                return new NormalBrick(x, y, width, height);
            case STRONG :
                return new StrongBrick(x, y, width, height);
            case UNBREAKABLE :
                return new UnbreakableBrick(x, y, width, height);

            default :
                return null;
        }
    }

    // Tạo loại gạch random
    public static Brick createRandomBrick(double x, double y, double width, double height) {
        Brick.BrickType[] types = {Brick.BrickType.NORMAL, Brick.BrickType.STRONG};
        Brick.BrickType randomType = types[(int) (Math.random() * types.length)];
        return createBrick(randomType, x, y, width, height);
    }
}