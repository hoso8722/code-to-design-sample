package exam;

public class TransferExam extends Exam {
    @Override
    protected PassingRule createRule() {
        return new TransferRule();
    }
}
