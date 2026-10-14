// 検証しない: 複数ファイルの例。collect.sh で java bench/Bench.java として実行している
import java.util.function.Predicate;

public class Bench {
    record Student(String name, int math, int english, int science) {
        int total() { return math + english + science; }
    }

    interface Rule { boolean passes(Student s); }
    static class GeneralRule implements Rule { public boolean passes(Student s) { return s.total() >= 180; } }
    static class TransferRule implements Rule { public boolean passes(Student s) { return s.math() + s.english() >= 120; } }

    static boolean branch(String type, Student s) {
        if (type.equals("general")) return s.total() >= 180;
        else if (type.equals("transfer")) return s.math() + s.english() >= 120;
        throw new IllegalArgumentException(type);
    }

    static final int N = 20_000_000;

    public static void main(String[] args) {
        Student[] students = new Student[1000];
        for (int i = 0; i < students.length; i++) students[i] = new Student("s" + i, i % 101, (i * 7) % 101, (i * 13) % 101);
        String[] types = {"general", "transfer"};
        Rule[] rules = {new GeneralRule(), new TransferRule()};
        Predicate<Student>[] lambdas = new Predicate[] {(Predicate<Student>) s -> s.total() >= 180, (Predicate<Student>) s -> s.math() + s.english() >= 120};
        for (int run = 1; run <= 5; run++) {
            int hits = 0;
            long t0 = System.nanoTime();
            for (int i = 0; i < N; i++) if (branch(types[i & 1], students[i % 1000])) hits++;
            long t1 = System.nanoTime();
            for (int i = 0; i < N; i++) if (rules[i & 1].passes(students[i % 1000])) hits++;
            long t2 = System.nanoTime();
            for (int i = 0; i < N; i++) if (lambdas[i & 1].test(students[i % 1000])) hits++;
            long t3 = System.nanoTime();
            System.out.printf("run %d: 条件分岐=%.2f ns, interface=%.2f ns, ラムダ=%.2f ns (hits=%d)%n", run,
                (t1 - t0) / (double) N, (t2 - t1) / (double) N, (t3 - t2) / (double) N, hits);
        }
    }
}
