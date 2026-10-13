// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class MissingStatic {
    int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println(add(3, 4));
    }
}
