// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class FixedLength {
    public static void main(String[] args) {
        int[] scores = {80, 65, 90};
        scores.length = 5;
    }
}
