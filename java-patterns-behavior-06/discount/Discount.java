// 検証しない: 複数ファイルの例。collect.sh で java discount/Discount.java として実行している
import java.util.List;
import java.util.Optional;

// 受験料の減額：最初に当てはまった規則が担当する（Chain of Responsibility の古典的な形）
public class Discount {
    record Applicant(String name, boolean scholarship, boolean sibling, boolean early) {}

    // 古典的な形：各担当が「自分が扱えるか」を決め、扱えなければ次の担当に渡す
    static abstract class Handler {
        private Handler next;

        Handler then(Handler next) {
            this.next = next;
            return next;
        }

        int fee(Applicant a) {
            Optional<Integer> mine = handle(a);
            if (mine.isPresent()) return mine.get();
            if (next == null) return 10_000;   // 誰も扱えなければ、定価
            return next.fee(a);
        }

        abstract Optional<Integer> handle(Applicant a);
    }

    static class ScholarshipHandler extends Handler {
        Optional<Integer> handle(Applicant a) { return a.scholarship() ? Optional.of(0) : Optional.empty(); }
    }

    static class SiblingHandler extends Handler {
        Optional<Integer> handle(Applicant a) { return a.sibling() ? Optional.of(5_000) : Optional.empty(); }
    }

    static class EarlyHandler extends Handler {
        Optional<Integer> handle(Applicant a) { return a.early() ? Optional.of(9_000) : Optional.empty(); }
    }

    // 関数のリストで書く形：最初に値を返した規則を使う
    record Rule(java.util.function.Predicate<Applicant> applies, int fee) {}

    static int feeByRules(List<Rule> rules, Applicant a) {
        return rules.stream().filter(r -> r.applies().test(a)).findFirst().map(Rule::fee).orElse(10_000);
    }

    public static void main(String[] args) {
        List<Applicant> applicants = List.of(
            new Applicant("佐藤（奨学金のみ）", true, false, false),
            new Applicant("鈴木（兄弟と早期）", false, true, true),
            new Applicant("高橋（早期のみ）", false, false, true),
            new Applicant("伊藤（なし）", false, false, false),
            new Applicant("渡辺（奨学金と兄弟）", true, true, false));

        Handler scholarshipFirst = new ScholarshipHandler();
        scholarshipFirst.then(new SiblingHandler()).then(new EarlyHandler());
        Handler earlyFirst = new EarlyHandler();
        earlyFirst.then(new SiblingHandler()).then(new ScholarshipHandler());

        List<Rule> rules = List.of(new Rule(Applicant::scholarship, 0), new Rule(Applicant::sibling, 5_000), new Rule(Applicant::early, 9_000));

        System.out.printf("%-22s %12s %12s %12s%n", "受験者", "奨学金が先", "早期が先", "リスト版");
        for (Applicant a : applicants) {
            System.out.printf("%-20s %10d円 %10d円 %10d円%n", a.name(), scholarshipFirst.fee(a), earlyFirst.fee(a), feeByRules(rules, a));
        }
    }
}
