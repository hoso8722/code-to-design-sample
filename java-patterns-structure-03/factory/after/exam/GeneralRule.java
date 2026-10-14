package exam;

class GeneralRule implements PassingRule {
    GeneralRule() {}

    @Override
    public boolean passes(Student student) {
        return student.total() >= 180;
    }

    @Override
    public String describe() {
        return "一般：3科目の合計が180点以上";
    }
}
