// 実行: java Warmup.java
public class Warmup {
    // 1,000個の数の「2乗の和」を計算する（1回の呼び出しで、ループが1,000回まわる）
    static long sumOfSquares(int[] values) {
        long sum = 0;
        for (int v : values) {
            sum += (long) v * v;
        }
        return sum;
    }

    public static void main(String[] args) {
        int[] values = new int[1_000];
        for (int i = 0; i < values.length; i++) values[i] = i;

        int calls = 20_000;
        long[] nanos = new long[calls];
        long check = 0;
        for (int i = 0; i < calls; i++) {
            long start = System.nanoTime();
            check += sumOfSquares(values);
            nanos[i] = System.nanoTime() - start;  // 1回の呼び出しにかかった時間
        }

        // 区間ごとの平均を出力する（1回ずつの値はばらつきが大きいため）
        System.out.println("from,to,avg_ns");
        int[] bounds = {0, 10, 100, 200, 500, 1_000, 2_000, 5_000, 10_000, 20_000};
        for (int b = 0; b + 1 < bounds.length; b++) {
            long total = 0;
            for (int i = bounds[b]; i < bounds[b + 1]; i++) total += nanos[i];
            System.out.printf("%d,%d,%d%n", bounds[b] + 1, bounds[b + 1], total / (bounds[b + 1] - bounds[b]));
        }
        System.out.println("check=" + check);  // 結果を使って、計算が消されないようにする
    }
}
