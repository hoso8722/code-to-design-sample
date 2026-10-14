// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;

// 状態を持つ判定ルール：「合格点に達した人のうち、先着3人まで」
public class Quota {
    record Student(String name, int total) {}

    // 1つのルールを、試験回をまたいで使い回してしまう例
    static final Predicate<Student> SHARED = new Predicate<>() {
        private final AtomicInteger accepted = new AtomicInteger();
        @Override public boolean test(Student s) {
            return s.total() >= 180 && accepted.incrementAndGet() <= 3;
        }
    };

    // 試験回ごとに、新しいルールを作る
    static Predicate<Student> newRule() {
        AtomicInteger accepted = new AtomicInteger();
        return s -> s.total() >= 180 && accepted.incrementAndGet() <= 3;
    }

    static List<String> run(Predicate<Student> rule, String label) {
        List<String> passed = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Student s = new Student(label + i, 200);
            if (rule.test(s)) passed.add(s.name());
        }
        return passed;
    }

    public static void main(String[] args) {
        System.out.println("使い回すルール 1回目: " + run(SHARED, "春"));
        System.out.println("使い回すルール 2回目: " + run(SHARED, "秋"));
        Supplier<Predicate<Student>> factory = Quota::newRule;
        System.out.println("回ごとに作るルール 1回目: " + run(factory.get(), "春"));
        System.out.println("回ごとに作るルール 2回目: " + run(factory.get(), "秋"));
    }
}
