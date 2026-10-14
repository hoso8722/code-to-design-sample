package exam;

class RecommendationRule implements PassingRule {
    @Override
    public boolean passes(Student s) { return s.total() >= 150 && s.english() >= 70; }
}
