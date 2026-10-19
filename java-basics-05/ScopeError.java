// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class ScopeError {
    public static void main(String[] args) {
        for (int i = 1; i <= 3; i++) {
            System.out.println(i);
        }
        System.out.println("最後の i = " + i);
    }
}
