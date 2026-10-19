// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class NewInterface {
    public static void main(String[] args) {
        Shape shape = new Shape();
    }
}

interface Shape {
    double area();
}
