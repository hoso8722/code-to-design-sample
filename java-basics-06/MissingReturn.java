// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class MissingReturn {
    static int grade(int score) {
        if (score >= 60) {
            return 1;
        }
    }

    public static void main(String[] args) {
        System.out.println(grade(80));
    }
}
