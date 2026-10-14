package exam;

public class ExamService {
    public boolean judge(String type, Student student) {
        PassingRule rule;
        if (type.equals("general")) {
            rule = new GeneralRule();
        } else if (type.equals("scholarship")) {
            rule = new ScholarshipRule();
        } else if (type.equals("transfer")) {
            rule = new TransferRule();
        } else if (type.equals("recommendation")) {
            rule = new RecommendationRule();
        } else {
            throw new IllegalArgumentException("unknown type: " + type);
        }
        return rule.passes(student);
    }
}
