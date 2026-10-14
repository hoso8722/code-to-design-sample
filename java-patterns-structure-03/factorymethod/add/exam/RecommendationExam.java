package exam;

public class RecommendationExam extends Exam {
    @Override
    protected PassingRule createRule() {
        return new RecommendationRule();
    }
}
