// 実行: java WarmupTrace.java
// Warmup.java と同じ計測で、1回ずつの時間をすべて出力する（グラフ用）
public class WarmupTrace {
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

        System.out.println("call,nanos");
        for (int i = 0; i < calls; i++) System.out.println((i + 1) + "," + nanos[i]);
        System.out.println("check=" + check);  // 結果を使って、計算が消されないようにする
    }
}
