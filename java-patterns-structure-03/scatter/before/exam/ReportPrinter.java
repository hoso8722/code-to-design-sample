package exam;

public class ReportPrinter {
    public String describe(String type) {
        PassingRule rule;
        if (type.equals("general")) {
            rule = new GeneralRule();
        } else if (type.equals("scholarship")) {
            rule = new ScholarshipRule();
        } else if (type.equals("transfer")) {
            rule = new TransferRule();
        } else {
            throw new IllegalArgumentException("unknown type: " + type);
        }
        return rule.describe();
    }
}
