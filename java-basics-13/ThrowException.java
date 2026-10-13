// 実行: java ThrowException.java
class InvalidScoreException extends Exception {          // 自作の検査例外
    InvalidScoreException(String message) {
        super(message);
    }
}

public class ThrowException {
    // throws: この例外を投げうることを宣言する(検査例外は、宣言か処理が必要)
    static void checkScore(int score) throws InvalidScoreException {
        if (score < 0 || score > 100) {
            throw new InvalidScoreException("点数は0〜100です: " + score);
        }
    }

    // 実行時の例外(非検査例外)は、宣言しなくてよい
    static void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("名前が空です");
        }
    }

    public static void main(String[] args) {
        try {
            checkScore(80);
            checkScore(105);
        } catch (InvalidScoreException e) {
            System.out.println("検査例外を捕まえた: " + e.getMessage());
        }

        try {
            checkName("");
        } catch (IllegalArgumentException e) {
            System.out.println("非検査例外を捕まえた: " + e.getMessage());
        }
    }
}
