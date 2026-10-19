// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
public class AssignInCondition {
    public static void main(String[] args) {
        int score = 80;
        if (score = 100) {
            System.out.println("満点");
        }
    }
}
