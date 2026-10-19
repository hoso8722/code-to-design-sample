// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class FinalClass {
    public static void main(String[] args) {
        new Child().hello();
    }
}

final class Base {
    void hello() {
        System.out.println("hello");
    }
}

class Child extends Base {
}
