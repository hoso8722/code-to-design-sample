// 実行: java Coordinate.java
public class Coordinate {
    public static void main(String[] args) {
        Point2D c = new Point2D(3, 4);
        System.out.println(c);
        System.out.println("原点からの距離: " + c.distanceFromOrigin());
    }
}

record Point2D(int x, int y) {
    double distanceFromOrigin() {
        return Math.sqrt(x * x + y * y);
    }
}
