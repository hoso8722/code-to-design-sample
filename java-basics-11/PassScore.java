// 実行: java PassScore.java
public class PassScore {
    public static void main(String[] args) {
        int[] scores = {75, 60, 59, 105};
        for (int score : scores) {
            if (!Rules.isValid(score)) {
                System.out.println(score + "点: 不正な点数");
            } else {
                System.out.println(score + "点: " + (Rules.isPassed(score) ? "合格" : "不合格"));
            }
        }
    }
}

class Rules {
    static final int PASS_SCORE = 60;
    static final int MAX_SCORE = 100;

    static boolean isPassed(int score) {
        return score >= PASS_SCORE;
    }

    static boolean isValid(int score) {
        return score >= 0 && score <= MAX_SCORE;
    }
}
