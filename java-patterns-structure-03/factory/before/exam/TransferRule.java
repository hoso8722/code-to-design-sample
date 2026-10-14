package exam;

class TransferRule implements PassingRule {
    TransferRule() {}

    @Override
    public boolean passes(Student student) {
        return student.math() + student.english() >= 120;
    }

    @Override
    public String describe() {
        return "編入：数学と英語の合計が120点以上";
    }
}
