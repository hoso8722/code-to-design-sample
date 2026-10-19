// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class BadOperand {
    public static void main(String[] args) {
        int score = 80;
        boolean inRange = 0 <= score <= 100;
        System.out.println(inRange);
    }
}
