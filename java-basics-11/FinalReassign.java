// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class FinalReassign {
    static final int MAX = 100;

    public static void main(String[] args) {
        final int limit = 10;
        limit = 20;
        MAX = 200;
        final int[] scores = {80, 65, 90};
        scores = new int[3];
    }
}
