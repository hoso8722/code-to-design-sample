// 検証しない: 実行時エラーを見せるための、わざと間違えたコード
public class IndexOutOfRange {
    public static void main(String[] args) {
        int[] scores = {80, 65, 90};
        System.out.println("先に表示される行");
        System.out.println(scores[3]);
    }
}
