// 検証しない: パッケージ付きのファイル。collect.sh で java bench/exam/Bench.java として実行している
package exam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Bench {
    record Student(String name, int math, int english, int science) { int total() { return math + english + science; } }
    interface Source { Student parse(String line); }
    static class V1 implements Source {
        public Student parse(String line) {
            String[] c = line.split(",");
            return new Student(c[0], Integer.parseInt(c[1]), Integer.parseInt(c[2]), Integer.parseInt(c[3]));
        }
    }

    static String direct(List<String> lines) {
        String best = null; int bestTotal = -1;
        for (String line : lines) {
            String[] c = line.split(",");
            int total = Integer.parseInt(c[1]) + Integer.parseInt(c[2]) + Integer.parseInt(c[3]);
            if (total > bestTotal) { bestTotal = total; best = c[0]; }
        }
        return best;
    }

    static String adapted(List<String> lines, Source source) {
        return lines.stream().map(source::parse).max(Comparator.comparingInt(Student::total)).map(Student::name).orElse(null);
    }

    public static void main(String[] args) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) lines.add("受験者" + i + "," + (i % 101) + "," + ((i * 7) % 101) + "," + ((i * 13) % 101));
        Source source = new V1();
        for (int run = 1; run <= 5; run++) {
            long t0 = System.nanoTime();
            String a = direct(lines);
            long t1 = System.nanoTime();
            String b = adapted(lines, source);
            long t2 = System.nanoTime();
            System.out.printf("run %d: 直接=%.0f ms, アダプタ経由=%.0f ms (%s, %s)%n", run, (t1 - t0) / 1e6, (t2 - t1) / 1e6, a, b);
        }
    }
}
