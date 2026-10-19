// 実行: java IfElse.java
public class IfElse {
    static String grade(int score) {
        if (score >= 90) {
            return "A";
        } else if (score >= 80) {
            return "B";
        } else if (score >= 60) {
            return "C";
        } else {
            return "D";
        }
    }

    public static void main(String[] args) {
        int[] scores = {95, 90, 89, 80, 79, 60, 59, 0};
        for (int score : scores) {
            System.out.println(score + "点 → " + grade(score));
        }
    }
}
