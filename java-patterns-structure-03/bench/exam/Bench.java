// 検証しない: パッケージ付きのファイル。collect.sh で java bench/exam/Bench.java として実行している
package exam;

import java.lang.reflect.Constructor;

public class Bench {
    interface Rule { boolean passes(int total); }
    static class GeneralRule implements Rule { public boolean passes(int total) { return total >= 180; } }
    static class TransferRule implements Rule { public boolean passes(int total) { return total >= 120; } }

    static Rule viaNew(int i) { return (i & 1) == 0 ? new GeneralRule() : new TransferRule(); }

    static Rule viaFactory(String type) {
        return switch (type) {
            case "general" -> new GeneralRule();
            case "transfer" -> new TransferRule();
            default -> throw new IllegalArgumentException(type);
        };
    }

    static Rule viaReflection(String className) throws Exception {
        return (Rule) Class.forName(className).getDeclaredConstructor().newInstance();
    }

    static final int N = 5_000_000;

    public static void main(String[] args) throws Exception {
        String[] types = {"general", "transfer"};
        String[] classes = {"exam.Bench$GeneralRule", "exam.Bench$TransferRule"};
        Constructor<?>[] cached = {GeneralRule.class.getDeclaredConstructor(), TransferRule.class.getDeclaredConstructor()};
        for (int run = 1; run <= 5; run++) {
            long hits = 0;
            long t0 = System.nanoTime();
            for (int i = 0; i < N; i++) if (viaNew(i).passes(i % 300)) hits++;
            long t1 = System.nanoTime();
            for (int i = 0; i < N; i++) if (viaFactory(types[i & 1]).passes(i % 300)) hits++;
            long t2 = System.nanoTime();
            for (int i = 0; i < N; i++) if (((Rule) cached[i & 1].newInstance()).passes(i % 300)) hits++;
            long t3 = System.nanoTime();
            for (int i = 0; i < N; i++) if (viaReflection(classes[i & 1]).passes(i % 300)) hits++;
            long t4 = System.nanoTime();
            System.out.printf("run %d: new=%.1f ns, 静的ファクトリ=%.1f ns, リフレクション(Constructor保持)=%.1f ns, リフレクション(毎回Class.forName)=%.1f ns (hits=%d)%n",
                run, (t1 - t0) / (double) N, (t2 - t1) / (double) N, (t3 - t2) / (double) N, (t4 - t3) / (double) N, hits);
        }
    }
}
