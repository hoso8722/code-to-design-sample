// 実行: java Deopt.java
public class Deopt {
    interface Shape { double area(); }
    record Square(double side) implements Shape { public double area() { return side * side; } }
    record Rect(double w, double h) implements Shape { public double area() { return w * h; } }

    static double total(Shape[] shapes) {
        double sum = 0;
        for (Shape s : shapes) sum += s.area();
        return sum;
    }

    static final long T0 = System.nanoTime();

    public static void main(String[] args) {
        Shape[] squares = new Shape[100_000];
        Shape[] mixed = new Shape[100_000];
        for (int i = 0; i < squares.length; i++) {
            squares[i] = new Square(i % 10);
            mixed[i] = (i % 2 == 0) ? new Square(i % 10) : new Rect(i % 10, 2);  // Square と Rect が半分ずつ
        }

        double check = 0;
        System.out.println("call,phase,start_us,micros");
        for (int call = 1; call <= 600; call++) {
            Shape[] input = call <= 300 ? squares : mixed;  // 301回目から、Rect も混ざる
            long start = System.nanoTime();
            check += total(input);
            long end = System.nanoTime();
            System.out.printf("%d,%s,%d,%d%n", call, call <= 300 ? "squares" : "mixed", (start - T0) / 1000, (end - start) / 1000);
        }
        if (check == 42) System.out.println();  // 結果を使って、計算が消されないようにする
    }
}
