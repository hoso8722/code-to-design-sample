// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
interface Shape {
    double area();
}

class Circle implements Shape {
}

public class MissingImplementation {
    public static void main(String[] args) {
        new Circle();
    }
}
