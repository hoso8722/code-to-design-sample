package exam;

public class GeneralExam extends Exam {
    @Override
    protected PassingRule createRule() {
        return new GeneralRule();
    }
}
