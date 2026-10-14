package exam;

public class ScholarshipExam extends Exam {
    @Override
    protected PassingRule createRule() {
        return new ScholarshipRule();
    }
}
