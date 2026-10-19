// 実行: java NewGrade.java
public class NewGrade {
    static String grade(int rank) {
        return switch (rank) {
            case 1 -> "金";
            case 2 -> "銀";
            case 3 -> "銅";
            default -> "入賞なし";
        };
    }

    public static void main(String[] args) {
        for (int rank = 1; rank <= 4; rank++) {
            System.out.println(rank + "位 → " + grade(rank));
        }
    }
}
