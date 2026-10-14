package exam;

class ScholarshipRule implements PassingRule {
    ScholarshipRule() {}

    @Override
    public boolean passes(Student student) {
        return student.total() / 3 >= 70 && student.math() >= 60 && student.english() >= 60 && student.science() >= 60;
    }

    @Override
    public String describe() {
        return "奨学：平均70点以上、各科目60点以上";
    }
}
