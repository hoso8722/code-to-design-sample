package exam;

// Abstract Factory：入試区分に合う「判定ルール」と「成績表」を、組にして作る
public interface ExamKit {
    PassingRule rule();
    Scorecard scorecard();

    ExamKit GENERAL = new ExamKit() {
        public PassingRule rule() { return s -> s.total() >= 180; }
        public Scorecard scorecard() { return s -> s.name() + " 合計=" + s.total(); }
    };

    ExamKit TRANSFER = new ExamKit() {
        public PassingRule rule() { return s -> s.math() + s.english() >= 120; }
        public Scorecard scorecard() { return s -> s.name() + " 数学+英語=" + (s.math() + s.english()); }
    };
}
