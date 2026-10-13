// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
final class Base {
    void hello() {
        System.out.println("hello");
    }
}

class Child extends Base {
}

public class FinalClass {
    public static void main(String[] args) {
        new Child().hello();
    }
}
