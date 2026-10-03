// 実行: java NoTrace.java
public class NoTrace {
    // スタックトレースを作らない例外
    static class NoTraceException extends RuntimeException {
        NoTraceException(String message) {
            super(message, null, false, false);
        }
    }

    static int parseScore(String text) {
        throw new NoTraceException("点数を数値にできません: " + text);
    }

    public static void main(String[] args) {
        try {
            parseScore("八十");
        } catch (NoTraceException e) {
            System.out.println("スタックトレースの行数: " + e.getStackTrace().length);
            e.printStackTrace(System.out);
        }
    }
}
