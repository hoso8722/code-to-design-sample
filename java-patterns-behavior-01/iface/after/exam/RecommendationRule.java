package exam;

class RecommendationRule implements PassingRule {
    @Override public boolean passes(Student s) { return s.total() >= 150 && s.english() >= 70; }
    @Override public String explain() { return "合計150点以上、英語70点以上"; }
}
