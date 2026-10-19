// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class WrongArguments {
    static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println(add(1));
        System.out.println(add("1", 2));
    }
}
