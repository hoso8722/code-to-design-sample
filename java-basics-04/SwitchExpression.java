// 実行: java SwitchExpression.java
public class SwitchExpression {
    static String gradeByTens(int score) {
        // 点数を10で割った商で分ける(100点は10、90点台は9)
        return switch (score / 10) {
            case 10, 9 -> "A";
            case 8 -> "B";
            case 7, 6 -> "C";
            default -> "D";
        };
    }

    static String describe(int score) {
        return switch (score / 10) {
            case 10, 9 -> "とてもよい";
            case 8, 7 -> {
                String message = "よい";   // 複数の文が必要なときはブロックにして yield で値を返す
                yield message;
            }
            default -> "もう少し";
        };
    }

    public static void main(String[] args) {
        for (int score : new int[] {100, 95, 85, 72, 60, 59}) {
            System.out.println(score + "点 → " + gradeByTens(score) + "、" + describe(score));
        }
    }
}
