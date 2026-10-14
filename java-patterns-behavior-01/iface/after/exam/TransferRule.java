package exam;

class TransferRule implements PassingRule {
    @Override public boolean passes(Student s) { return s.math() + s.english() >= 120; }
    @Override public String explain() { return "数学と英語の合計120点以上"; }
}
