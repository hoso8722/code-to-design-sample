package exam;

final class Clock {
    private static final long START = System.nanoTime();

    private Clock() {}

    // 起動からの経過時間（ミリ秒）つきで表示する。10ミリ秒単位に丸める
    static void log(String message) {
        long ms = (System.nanoTime() - START) / 1_000_000 / 10 * 10;
        System.out.printf("[%4d ms] %s%n", ms, message);
    }
}
