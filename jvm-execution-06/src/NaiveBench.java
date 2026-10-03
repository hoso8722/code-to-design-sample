// 実行: java NaiveBench.java plus-first
public class NaiveBench {
    static String plusLoop(int n) {          // + で1つずつつなぐ
        String s = "";
        for (int i = 0; i < n; i++) s += i;
        return s;
    }

    static String builder(int n) {           // StringBuilder でつなぐ
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(i);
        return sb.toString();
    }

    // 1. 結果を使わない計算を計る
    static void deadCode() {
        long start = System.nanoTime();
        for (int i = 0; i < 1_000_000_000; i++) {
            long x = (long) i * i % 7;  // 計算した結果を捨てている
        }
        long unused = System.nanoTime() - start;

        long sum = 0;
        start = System.nanoTime();
        for (int i = 0; i < 1_000_000_000; i++) {
            sum += (long) i * i % 7;    // 計算した結果を使う
        }
        long used = System.nanoTime() - start;
        System.out.printf("10億回の計算：結果を捨てる %d ms、結果を使う %d ms（sum=%d）%n",
                unused / 1_000_000, used / 1_000_000, sum);
    }

    // 2. 2つの方法を、同じJVMの中で順番に計る
    static long timeOf(String name, java.util.function.IntFunction<String> f) {
        long start = System.nanoTime();
        int length = 0;
        for (int i = 0; i < 2_000; i++) length += f.apply(100).length();
        long micros = (System.nanoTime() - start) / 1_000;
        System.out.printf("  %-8s 2,000回で %,d µs（length=%d）%n", name, micros, length);
        return micros;
    }

    public static void main(String[] args) {
        deadCode();
        boolean plusFirst = args.length == 0 || args[0].equals("plus-first");
        System.out.println(plusFirst ? "plusLoop → builder の順に計る" : "builder → plusLoop の順に計る");
        if (plusFirst) {
            timeOf("plusLoop", NaiveBench::plusLoop);
            timeOf("builder", NaiveBench::builder);
        } else {
            timeOf("builder", NaiveBench::builder);
            timeOf("plusLoop", NaiveBench::plusLoop);
        }
    }
}
