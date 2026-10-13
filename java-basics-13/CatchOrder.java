// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class CatchOrder {
    public static void main(String[] args) {
        try {
            Integer.parseInt("abc");
        } catch (RuntimeException e) {
            System.out.println("実行時の例外");
        } catch (NumberFormatException e) {
            System.out.println("数値ではない");
        }
    }
}
