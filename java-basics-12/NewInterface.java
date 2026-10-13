// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
interface Shape {
    double area();
}

public class NewInterface {
    public static void main(String[] args) {
        Shape shape = new Shape();
    }
}
