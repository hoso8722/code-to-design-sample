// 検証しない: 複数ファイルの例。collect.sh で java same/Same.java として実行している
import java.util.function.Predicate;

// 似た形に見えるパターンの違い：「誰が」「いつ」振る舞いを切り替えるか
public class Same {
    record Student(String name, int total) {}

    // 1. Strategy：呼び出す側が、ルールを選んで渡す
    static class StrategyExaminer {
        private Predicate<Student> rule;
        StrategyExaminer(Predicate<Student> rule) { this.rule = rule; }
        void setRule(Predicate<Student> rule) { this.rule = rule; }   // 外から差し替える
        boolean judge(Student s) { return rule.test(s); }
    }

    // 2. Template Method：流れは親、ルールは子クラス。オブジェクトを作るときに決まる
    static abstract class TemplateExaminer {
        final String judge(Student s) { return "[" + label() + "] " + (passes(s) ? "合格" : "不合格"); }   // 流れは固定
        abstract boolean passes(Student s);
        abstract String label();
    }
    static class NormalExaminer extends TemplateExaminer {
        boolean passes(Student s) { return s.total() >= 180; }
        String label() { return "通常"; }
    }
    static class StrictExaminer extends TemplateExaminer {
        boolean passes(Student s) { return s.total() >= 200; }
        String label() { return "厳格"; }
    }

    // 3. State：オブジェクト自身が、判定の回数に応じて、自分で状態を切り替える
    interface State { boolean passes(Student s); String label(); }
    static class StateExaminer {
        private State state = new State() {
            public boolean passes(Student s) { return s.total() >= 180; }
            public String label() { return "通常"; }
        };
        private int judged;

        String judge(Student s) {
            boolean result = state.passes(s);
            String line = "[" + state.label() + "] " + (result ? "合格" : "不合格");
            if (++judged == 2) {   // 2回判定したら、厳格な状態に自分で移る
                state = new State() {
                    public boolean passes(Student s2) { return s2.total() >= 200; }
                    public String label() { return "厳格"; }
                };
            }
            return line;
        }
    }

    public static void main(String[] args) {
        Student s = new Student("佐藤", 190);

        System.out.println("-- Strategy（外から setRule で差し替える）");
        StrategyExaminer strategy = new StrategyExaminer(x -> x.total() >= 180);
        System.out.println("1回目: " + (strategy.judge(s) ? "合格" : "不合格"));
        strategy.setRule(x -> x.total() >= 200);
        System.out.println("差し替えたあと: " + (strategy.judge(s) ? "合格" : "不合格"));

        System.out.println("-- Template Method（作ったオブジェクトの種類は、変えられない）");
        TemplateExaminer template = new NormalExaminer();
        System.out.println("1回目: " + template.judge(s));
        System.out.println("2回目: " + template.judge(s) + "（同じオブジェクトのまま。厳格にするには、新しく作り直す）");
        TemplateExaminer strict = new StrictExaminer();
        System.out.println("作り直した: " + strict.judge(s));

        System.out.println("-- State（オブジェクト自身が、2回判定したら厳格な状態に移る）");
        StateExaminer state = new StateExaminer();
        for (int i = 1; i <= 3; i++) System.out.println(i + "回目: " + state.judge(s));
    }
}
