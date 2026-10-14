package exam;

class GeneralRule implements PassingRule {
    @Override
    public boolean passes(Student s) { return s.total() >= 180; }
}
