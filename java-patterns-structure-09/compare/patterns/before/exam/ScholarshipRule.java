package exam;

class ScholarshipRule implements PassingRule {
    @Override
    public boolean passes(Student s) {
        return s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60;
    }
}
