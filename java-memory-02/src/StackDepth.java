// 実行: java -Xss1m StackDepth.java
public class StackDepth {
    static int depth = 0;

    // ローカル変数が少ないメソッド
    static void small() {
        depth++;
        small();
    }

    // ローカル変数（long が8個）が多いメソッド
    static long large(long a) {
        depth++;
        long b = a + 1, c = b + 1, d = c + 1, e = d + 1, f = e + 1, g = f + 1, h = g + 1;
        return large(h) + a + b + c + d + e + f + g + h;
    }

    static int measure(Runnable r) {
        depth = 0;
        try {
            r.run();
        } catch (StackOverflowError e) {
            // スタックが足りなくなったところで止まる
        }
        return depth;
    }

    public static void main(String[] args) {
        System.out.println("small: " + measure(StackDepth::small));
        System.out.println("large: " + measure(() -> large(0)));
    }
}
