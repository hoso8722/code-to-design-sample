package exam;

public class RecommendationRule implements PassingRule {
    @Override
    public boolean passes(Student student) {
        return student.total() >= 150 && student.english() >= 70;
    }

    @Override
    public String describe() {
        return "推薦：3科目の合計が150点以上、英語が70点以上";
    }
}
