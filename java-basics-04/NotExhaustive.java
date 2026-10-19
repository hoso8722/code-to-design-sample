// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class NotExhaustive {
    public static void main(String[] args) {
        int score = 85;
        String grade = switch (score / 10) {
            case 10, 9 -> "A";
            case 8 -> "B";
        };
        System.out.println(grade);
    }
}
