package exam;

class GeneralRule implements PassingRule {
    @Override public boolean passes(Student s) { return s.total() >= 180; }
    @Override public String explain() { return "合計180点以上"; }
}
