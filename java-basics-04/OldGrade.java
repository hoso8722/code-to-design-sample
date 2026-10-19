// 実行: java OldGrade.java
public class OldGrade {
    static String grade(int rank) {
        String result;
        switch (rank) {
            case 1:
                result = "金";
                break;
            case 2:
                result = "銀";
                break;
            case 3:
                result = "銅";
                break;
            default:
                result = "入賞なし";
                break;
        }
        return result;
    }

    public static void main(String[] args) {
        for (int rank = 1; rank <= 4; rank++) {
            System.out.println(rank + "位 → " + grade(rank));
        }
    }
}
