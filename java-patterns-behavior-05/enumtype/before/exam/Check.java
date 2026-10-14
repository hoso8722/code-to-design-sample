// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// 各状態で、各操作が許されるかを表にする（状態は、操作を進めて作る）
public class Check {
    interface Op { String name(); void apply(Procedure p); }

    static List<Op> ops() {
        List<Op> ops = new ArrayList<>();
        ops.add(op("take", p -> p.take()));
        ops.add(op("grade", p -> p.grade(80)));
        ops.add(op("announce", p -> p.announce()));
        return ops;
    }

    static Op op(String name, Consumer<Procedure> action) {
        return new Op() {
            public String name() { return name; }
            public void apply(Procedure p) { action.accept(p); }
        };
    }

    // 状態に至る操作の列
    static final String[][] PATHS = {{}, {"take"}, {"take", "grade"}, {"take", "grade", "announce"}};

    public static void main(String[] args) {
        List<Op> ops = ops();
        for (String[] path : PATHS) {
            StringBuilder line = new StringBuilder();
            Procedure sample = new Procedure();
            for (String step : path) apply(ops, sample, step);
            line.append(String.format("%-10s", sample.name())).append(" |");
            for (Op op : ops) {
                Procedure p = new Procedure();
                for (String step : path) apply(ops, p, step);
                String result;
                try {
                    op.apply(p);
                    result = "OK(" + p.name() + ")";
                } catch (IllegalStateException e) {
                    result = "拒否";
                }
                line.append(String.format(" %s=%-14s", op.name(), result));
            }
            System.out.println(line.toString().stripTrailing());
        }
        Procedure p = new Procedure();
        p.take(); p.grade(80); p.announce();
        System.out.println("ラベル: " + p.label());
    }

    static void apply(List<Op> ops, Procedure p, String step) {
        for (Op op : ops) if (op.name().equals(step)) { op.apply(p); return; }
    }
}
