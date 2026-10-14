package exam;

class TransferRule implements PassingRule {
    @Override
    public boolean passes(Student s) { return s.math() + s.english() >= 120; }
}
