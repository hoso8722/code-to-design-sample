// 実行: java InterfaceDemo.java
public class InterfaceDemo {
    public static void main(String[] args) {
        // interface の型の変数に、異なる実装のオブジェクトを入れられる
        Shape[] shapes = {new Circle(1.0), new Rectangle(2.0, 3.0)};
        double total = 0;
        for (Shape shape : shapes) {
            System.out.println(shape.describe());
            total += shape.area();
        }
        System.out.println("面積の合計: " + String.format("%.2f", total));

        // 型を調べて、その型として扱う(パターンマッチング)
        for (Shape shape : shapes) {
            if (shape instanceof Circle c) {
                System.out.println("円を見つけた: " + c.area());
            }
        }
    }
}

interface Shape {
    double area();                       // 実装のないメソッド(約束)

    default String describe() {          // default メソッド: interface 自身が持つ実装
        return getClass().getSimpleName() + " の面積: " + String.format("%.2f", area());
    }
}

class Circle implements Shape {
    private final double radius;

    Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

class Rectangle implements Shape {
    private final double width;
    private final double height;

    Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}
