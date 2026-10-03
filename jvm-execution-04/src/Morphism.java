// 実行: java Morphism.java 1
public class Morphism {
    interface Shape { double area(); }
    record Square(double side) implements Shape { public double area() { return side * side; } }
    record Rect(double w, double h) implements Shape { public double area() { return w * h; } }
    record Circle(double r) implements Shape { public double area() { return 3.14159 * r * r; } }
    record Triangle(double b, double h) implements Shape { public double area() { return 0.5 * b * h; } }

    // すべての図形の面積を足す。s.area() の呼び出し先は、実行するまで分からない
    static double total(Shape[] shapes) {
        double sum = 0;
        for (Shape s : shapes) sum += s.area();
        return sum;
    }

    public static void main(String[] args) {
        int kinds = args.length > 0 ? Integer.parseInt(args[0]) : 1;  // 使う図形の種類の数（1〜4）
        Shape[] shapes = new Shape[1_000_000];
        for (int i = 0; i < shapes.length; i++) {
            shapes[i] = switch (i % kinds) {  // 種類を順番に並べる
                case 0 -> new Square(i % 10);
                case 1 -> new Rect(i % 10, 2);
                case 2 -> new Circle(i % 10);
                default -> new Triangle(i % 10, 3);
            };
        }
        double check = 0;
        for (int i = 0; i < 20; i++) check += total(shapes);  // 準備運動（JITコンパイルさせる）

        long start = System.nanoTime();
        for (int i = 0; i < 50; i++) check += total(shapes);
        double nsPerCall = (System.nanoTime() - start) / (50.0 * shapes.length);
        System.out.printf("%d,%.2f%n", kinds, nsPerCall);
        if (check == 42) System.out.println();  // 結果を使って、計算が消されないようにする
    }
}
