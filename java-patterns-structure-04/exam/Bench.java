// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class Bench {
    static final int N = 5_000_000;

    public static void main(String[] args) {
        for (int run = 1; run <= 5; run++) {
            long sum = 0;
            long t0 = System.nanoTime();
            for (int i = 0; i < N; i++) {
                sum += new Application("受験者", i % 101, 70, 60, 5, 1, false, false).score();
            }
            long t1 = System.nanoTime();
            for (int i = 0; i < N; i++) {
                sum += new ApplicationBuilder().name("受験者").math(i % 101).english(70).science(60).bonus(5).build().score();
            }
            long t2 = System.nanoTime();
            System.out.printf("run %d: コンストラクタ=%.1f ns, Builder=%.1f ns (sum=%d)%n", run, (t1 - t0) / (double) N, (t2 - t1) / (double) N, sum);
        }
    }
}
