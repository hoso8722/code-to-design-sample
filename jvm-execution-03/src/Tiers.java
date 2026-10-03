// 実行: java -XX:+PrintCompilation Tiers.java
public class Tiers {
    // 文字列から数字だけを取り出して足す（1回の呼び出しは短い）
    static int digitSum(String s) {
        int sum = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') sum += c - '0';
        }
        return sum;
    }

    // 1,000個の数の2乗の和（1回の呼び出しで、ループが1,000回まわる）
    static long sumOfSquares(int[] values) {
        long sum = 0;
        for (int v : values) sum += (long) v * v;
        return sum;
    }

    public static void main(String[] args) {
        int[] values = new int[1_000];
        for (int i = 0; i < values.length; i++) values[i] = i;
        String text = "order-2026-10-01-A7-x3";

        long start = System.nanoTime();
        long check = 0;
        for (int round = 0; round < 200_000; round++) {
            check += sumOfSquares(values);
            for (int k = 0; k < 10; k++) check += digitSum(text);
        }
        System.out.printf("処理時間: %d ms（check=%d）%n", (System.nanoTime() - start) / 1_000_000, check);
    }
}
